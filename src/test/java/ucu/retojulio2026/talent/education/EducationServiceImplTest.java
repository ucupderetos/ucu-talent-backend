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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
                "Universidad Católica del Uruguay",
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
                "Universidad Católica del Uruguay",
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
    void crear_falla_si_la_carrera_no_es_de_ucu_y_no_se_indica_institucion() {
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
    void crear_guarda_la_educacion_si_los_datos_son_validos() {
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
        education.setStudentProfileId("student-1");
        education.setDegreeLevel(Education.DegreeLevel.LICENCIATURA);
        education.setDegreeId("degree-1");

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
    void buscar_por_id_falla_si_la_educacion_no_existe() {
        String educationId = "education-1";

        when(educationRepository.findById(educationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.getByEducationId(educationId)
        );

        verify(educationRepository).findById(educationId);
    }

    @Test
    void actualizar_modifica_los_campos_y_guarda_la_educacion() {
        String educationId = "education-1";

        Education existing = new Education();
        existing.setEducationId(educationId);
        existing.setStudentProfileId("student-anterior");
        existing.setDegreeLevel(Education.DegreeLevel.TECNICATURA);
        existing.setDegreeId("degree-anterior");
        existing.setInstitution("Institución anterior");
        existing.setDescription("Descripción anterior");
        existing.setStartDate(LocalDate.of(2020, 3, 1));
        existing.setEndDate(LocalDate.of(2022, 12, 1));

        UpdateEducationRequest request = new UpdateEducationRequest(
                "student-1",
                Education.DegreeLevel.LICENCIATURA,
                "degree-1",
                "Universidad ORT Uruguay",
                "Nueva descripción",
                LocalDate.of(2023, 3, 1),
                LocalDate.of(2026, 12, 1)
        );

        when(educationRepository.findById(educationId))
                .thenReturn(Optional.of(existing));

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(degreeRepository.findById("degree-1"))
                .thenReturn(Optional.of(degree));

        when(degree.getIsUcu()).thenReturn(false);

        when(educationRepository.save(existing))
                .thenReturn(existing);

        Education result = educationService.update(
                educationId,
                request
        );

        assertEquals("student-1", result.getStudentProfileId());
        assertEquals(
                Education.DegreeLevel.LICENCIATURA,
                result.getDegreeLevel()
        );
        assertEquals("degree-1", result.getDegreeId());
        assertEquals(
                "Universidad ORT Uruguay",
                result.getInstitution()
        );
        assertEquals(
                "Nueva descripción",
                result.getDescription()
        );
        assertEquals(
                LocalDate.of(2023, 3, 1),
                result.getStartDate()
        );
        assertEquals(
                LocalDate.of(2026, 12, 1),
                result.getEndDate()
        );

        verify(educationRepository).save(existing);
    }

    @Test
    void eliminar_borra_la_educacion_si_existe() {
        String educationId = "education-1";

        Education existing = new Education();
        existing.setEducationId(educationId);

        when(educationRepository.findById(educationId))
                .thenReturn(Optional.of(existing));

        educationService.delete(educationId);

        verify(educationRepository).findById(educationId);
        verify(educationRepository).delete(existing);
    }

    @Test
    void listar_por_perfil_devuelve_las_educaciones_del_estudiante() {
        String studentProfileId = "student-1";

        Education education1 = new Education();
        Education education2 = new Education();

        when(educationRepository.findByStudentProfileId(studentProfileId))
                .thenReturn(List.of(education1, education2));

        List<Education> result =
                educationService.getByStudentProfileId(studentProfileId);

        assertEquals(2, result.size());
        assertSame(education1, result.get(0));
        assertSame(education2, result.get(1));

        verify(educationRepository)
                .findByStudentProfileId(studentProfileId);
    }
}