package ucu.retojulio2026.talent.degree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucu.retojulio2026.talent.area.Area;
import ucu.retojulio2026.talent.area.AreaService;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.degree.dto.CreateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.DegreeMapper;
import ucu.retojulio2026.talent.degree.dto.UpdateDegreeRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DegreeServiceImplTest {

    @Mock
    private DegreeRepository degreeRepository;

    @Mock
    private DegreeMapper degreeMapper;

    @Mock
    private AreaService areaService;

    private DegreeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DegreeServiceImpl(degreeRepository, degreeMapper, areaService);
    }

    private Area existingArea() {
        Area area = new Area();
        area.setAreaId("area-1");
        area.setName("Tecnologia");
        area.setParentAreaId(null);
        return area;
    }

    private Degree degreeWithName(String name) {
        Degree degree = new Degree();
        degree.setDegreeId("degree-1");
        degree.setAreaId("area-1");
        degree.setName(name);
        degree.setIsUcu(true);
        return degree;
    }

    @Test
    void crear_carrera_falla_si_el_area_no_existe_y_propaga_resource_not_found() {
        CreateDegreeRequest request = new CreateDegreeRequest(
                "area-404",
                "Ingenieria en Informatica",
                true
        );

        ResourceNotFoundException exception = new ResourceNotFoundException("Area no encontrada");

        when(areaService.getById("area-404")).thenThrow(exception);

        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> service.create(request)
        );

        assertEquals(exception, thrown);

        verify(areaService).getById("area-404");
        verifyNoInteractions(degreeMapper);
        verify(degreeRepository, never()).save(any(Degree.class));
    }

    @Test
    void crear_carrera_normaliza_nombre_con_trim_y_primera_letra_mayuscula() {
        CreateDegreeRequest request = new CreateDegreeRequest(
                "area-1",
                "  base de datos",
                true
        );

        Degree mappedDegree = degreeWithName("  base de datos");

        when(areaService.getById("area-1")).thenReturn(existingArea());
        when(degreeMapper.toEntity(request)).thenReturn(mappedDegree);
        when(degreeRepository.save(any(Degree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Degree created = service.create(request);

        assertEquals("Base de datos", created.getName());
        assertEquals("area-1", created.getAreaId());
        assertEquals(true, created.getIsUcu());

        verify(areaService).getById("area-1");
        verify(degreeMapper).toEntity(request);
        verify(degreeRepository).save(mappedDegree);
    }

    @Test
    void editar_carrera_normaliza_nombre_con_trim_y_primera_letra_mayuscula() {
        Degree existing = degreeWithName("Nombre anterior");

        UpdateDegreeRequest request = new UpdateDegreeRequest(
                "area-2",
                "  base de datos",
                false
        );

        when(degreeRepository.findById("degree-1")).thenReturn(Optional.of(existing));
        when(degreeRepository.save(any(Degree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Degree updated = service.update("degree-1", request);

        assertEquals("area-2", updated.getAreaId());
        assertEquals("Base de datos", updated.getName());
        assertEquals(false, updated.getIsUcu());

        verify(degreeRepository).findById("degree-1");
        verify(degreeRepository).save(existing);
    }

    @Test
    void crear_carrera_deja_nombre_null_como_null() {
        CreateDegreeRequest request = new CreateDegreeRequest(
                "area-1",
                null,
                true
        );

        Degree mappedDegree = degreeWithName(null);

        when(areaService.getById("area-1")).thenReturn(existingArea());
        when(degreeMapper.toEntity(request)).thenReturn(mappedDegree);
        when(degreeRepository.save(any(Degree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Degree created = service.create(request);

        assertNull(created.getName());

        verify(areaService).getById("area-1");
        verify(degreeMapper).toEntity(request);
        verify(degreeRepository).save(mappedDegree);
    }

    @Test
    void crear_carrera_deja_string_vacio_como_vacio() {
        CreateDegreeRequest request = new CreateDegreeRequest(
                "area-1",
                "   ",
                true
        );

        Degree mappedDegree = degreeWithName("   ");

        when(areaService.getById("area-1")).thenReturn(existingArea());
        when(degreeMapper.toEntity(request)).thenReturn(mappedDegree);
        when(degreeRepository.save(any(Degree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Degree created = service.create(request);

        assertEquals("", created.getName());

        verify(areaService).getById("area-1");
        verify(degreeMapper).toEntity(request);
        verify(degreeRepository).save(mappedDegree);
    }

    @Test
    void crear_carrera_no_modifica_nombre_ya_capitalizado() {
        CreateDegreeRequest request = new CreateDegreeRequest(
                "area-1",
                "Base de datos",
                true
        );

        Degree mappedDegree = degreeWithName("Base de datos");

        when(areaService.getById("area-1")).thenReturn(existingArea());
        when(degreeMapper.toEntity(request)).thenReturn(mappedDegree);
        when(degreeRepository.save(any(Degree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Degree created = service.create(request);

        assertEquals("Base de datos", created.getName());

        verify(areaService).getById("area-1");
        verify(degreeMapper).toEntity(request);
        verify(degreeRepository).save(mappedDegree);
    }
}