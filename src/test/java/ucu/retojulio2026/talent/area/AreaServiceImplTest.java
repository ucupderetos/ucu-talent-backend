package ucu.retojulio2026.talent.area;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucu.retojulio2026.talent.area.dto.AreaMapper;
import ucu.retojulio2026.talent.area.dto.CreateAreaRequest;
import ucu.retojulio2026.talent.area.dto.UpdateAreaRequest;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaServiceImplTest {

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private AreaMapper areaMapper;

    private AreaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AreaServiceImpl(areaRepository, areaMapper);
    }

    private Area rootArea() {
        Area area = new Area();
        area.setAreaId("area-1");
        area.setName("Tecnologia");
        area.setParentAreaId(null);
        return area;
    }

    private Area childArea() {
        Area area = new Area();
        area.setAreaId("area-1");
        area.setName("Backend");
        area.setParentAreaId("parent-1");
        return area;
    }

    @Test
    void crear_area_falla_si_el_area_padre_no_existe() {
        CreateAreaRequest request = new CreateAreaRequest(
                "Backend",
                "parent-404"
        );

        when(areaRepository.existsById("parent-404")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));

        verify(areaRepository).existsById("parent-404");
        verifyNoInteractions(areaMapper);
        verify(areaRepository, never()).save(any(Area.class));
    }

    @Test
    void crear_area_raiz_guarda_ok_si_no_tiene_area_padre() {
        CreateAreaRequest request = new CreateAreaRequest(
                "Tecnologia",
                null
        );

        Area mappedArea = rootArea();

        when(areaMapper.toEntity(request)).thenReturn(mappedArea);
        when(areaRepository.save(any(Area.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Area created = service.create(request);

        assertEquals("Tecnologia", created.getName());
        assertEquals(null, created.getParentAreaId());

        verify(areaRepository, never()).existsById(anyString());
        verify(areaMapper).toEntity(request);
        verify(areaRepository).save(mappedArea);
    }

    @Test
    void editar_area_falla_si_el_area_padre_no_existe() {
        Area existing = rootArea();

        UpdateAreaRequest request = new UpdateAreaRequest(
                "Backend",
                "parent-404"
        );

        when(areaRepository.findById("area-1")).thenReturn(Optional.of(existing));
        when(areaRepository.existsById("parent-404")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.update("area-1", request));

        verify(areaRepository).findById("area-1");
        verify(areaRepository).existsById("parent-404");
        verify(areaRepository, never()).save(any(Area.class));
    }

    @Test
    void borrar_area_falla_si_no_existe_y_no_llama_a_deleteById() {
        when(areaRepository.existsById("area-404")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete("area-404"));

        verify(areaRepository).existsById("area-404");
        verify(areaRepository, never()).deleteById(anyString());
    }
}