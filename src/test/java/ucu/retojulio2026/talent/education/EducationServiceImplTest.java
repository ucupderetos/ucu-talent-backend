package ucu.retojulio2026.talent.education;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.degree.Degree;
import ucu.retojulio2026.talent.degree.DegreeRepository;
import ucu.retojulio2026.talent.education.dto.CreateEducationRequest;
import ucu.retojulio2026.talent.education.dto.EducationMapper;
import ucu.retojulio2026.talent.education.dto.UpdateEducationRequest;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EducationServiceImplTest {

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private EducationMapper educationMapper;

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private DegreeRepository degreeRepository;

    @Mock
    private Degree degree;

    private EducationServiceImpl educationService;

    @BeforeEach
    void setUp() {
        educationService = new EducationServiceImpl(
                educationRepository,
                educationMapper,
                studentProfileService,
                degreeRepository
        );
    }

    @Test
    void crear_falla_si_el_perfil_de_estudiante_no_existe() {
        CreateEducationRequest request = new CreateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                null,
                "Licenciatura en Informática",
                LocalDate.of(2024, 3, 1),
                null
        );

        when(studentProfileService.existsById("student-1"))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.create(request)
        );

        verify(degreeRepository, never()).findById(any());
        verify(educationMapper, never()).toEntity(any());
        verify(educationRepository, never()).save(any());
    }

    @Test
    void crear_falla_si_la_carrera_no_existe() {
        CreateEducationRequest request = new CreateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                null,
                "Licenciatura en Informática",
                LocalDate.of(2024, 3, 1),
                null
        );

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.create(request)
        );

        verify(educationMapper, never()).toEntity(any());
        verify(educationRepository, never()).save(any());
    }

    @Test
    void crear_falla_si_la_carrera_no_ucu_no_tiene_institucion() {
        CreateEducationRequest request = new CreateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                " ",
                "Licenciatura en Informática",
                LocalDate.of(2024, 3, 1),
                null
        );

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.of(degree));

        when(degree.getIsUcu()).thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> educationService.create(request)
        );

        verify(educationMapper, never()).toEntity(any());
        verify(educationRepository, never()).save(any());
    }

    @Test
    void crear_carrera_no_ucu_con_institucion_guarda_correctamente() {
        CreateEducationRequest request = new CreateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                "Universidad ORT Uruguay",
                "Licenciatura en Sistemas",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2028, 12, 1)
        );

        Education education = new Education();

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.of(degree));

        when(degree.getIsUcu()).thenReturn(false);

        when(educationMapper.toEntity(request))
                .thenReturn(education);

        when(educationRepository.save(education))
                .thenReturn(education);

        Education result = educationService.create(request);

        assertSame(education, result);

        verify(educationMapper).toEntity(request);
        verify(educationRepository).save(education);
    }

    @Test
    void crear_carrera_ucu_sin_institucion_guarda_correctamente() {
        CreateEducationRequest request = new CreateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                null,
                "Licenciatura en Informática",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2028, 12, 1)
        );

        Education education = new Education();

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.of(degree));

        when(degree.getIsUcu()).thenReturn(true);

        when(educationMapper.toEntity(request))
                .thenReturn(education);

        when(educationRepository.save(education))
                .thenReturn(education);

        Education result = educationService.create(request);

        assertSame(education, result);

        verify(educationMapper).toEntity(request);
        verify(educationRepository).save(education);
    }

    @Test
    void crear_falla_si_la_fecha_de_fin_es_anterior_a_la_fecha_de_inicio() {
        CreateEducationRequest request = new CreateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                null,
                "Licenciatura en Informática",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2023, 12, 1)
        );

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.of(degree));

        when(degree.getIsUcu()).thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> educationService.create(request)
        );

        verify(educationMapper, never()).toEntity(any());
        verify(educationRepository, never()).save(any());
    }

    @Test
    void crear_con_fecha_de_fin_null_guarda_como_en_curso() {
        CreateEducationRequest request = new CreateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                null,
                "Licenciatura en Informática",
                LocalDate.of(2024, 3, 1),
                null
        );

        Education education = new Education();
        education.setEndDate(null);

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.of(degree));

        when(degree.getIsUcu()).thenReturn(true);

        when(educationMapper.toEntity(request))
                .thenReturn(education);

        when(educationRepository.save(education))
                .thenReturn(education);

        Education result = educationService.create(request);

        assertEquals(null, result.getEndDate());

        verify(educationMapper).toEntity(request);
        verify(educationRepository).save(education);
    }

    @Test
    void crear_falla_si_la_fecha_de_inicio_es_anterior_a_1900() {
        CreateEducationRequest request = new CreateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                null,
                "Licenciatura en Informática",
                LocalDate.of(1899, 12, 31),
                null
        );

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.of(degree));

        when(degree.getIsUcu()).thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> educationService.create(request)
        );

        verify(educationMapper, never()).toEntity(any());
        verify(educationRepository, never()).save(any());
    }

    @Test
    void actualizar_revalida_alumno_carrera_y_fechas_antes_de_guardar() {
        String educationId = "education-1";

        Education existing = new Education();
        existing.setEducationId(educationId);

        UpdateEducationRequest request = new UpdateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                null,
                "Licenciatura en Informática",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2023, 12, 1)
        );

        when(educationRepository.findById(educationId))
                .thenReturn(Optional.of(existing));

        when(studentProfileService.existsById("student-1"))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.update(educationId, request)
        );

        verify(degreeRepository, never()).findById(any());
        verify(educationRepository, never()).save(any());

        reset(
                educationRepository,
                studentProfileService,
                degreeRepository,
                degree
        );

        when(educationRepository.findById(educationId))
                .thenReturn(Optional.of(existing));

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.update(educationId, request)
        );

        verify(educationRepository, never()).save(any());

        reset(
                educationRepository,
                studentProfileService,
                degreeRepository,
                degree
        );

        when(educationRepository.findById(educationId))
                .thenReturn(Optional.of(existing));

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.of(degree));

        when(degree.getIsUcu()).thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> educationService.update(educationId, request)
        );

        verify(studentProfileService).existsById("student-1");
        verify(degreeRepository).findById("degree-1");
        verify(educationRepository, never()).save(any());
    }

    @Test
    void eliminar_falla_si_la_educacion_no_existe() {
        String educationId = "education-1";

        when(educationRepository.findById(educationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.delete(educationId)
        );

        verify(educationRepository).findById(educationId);
        verify(educationRepository, never()).delete(any());
    }
}