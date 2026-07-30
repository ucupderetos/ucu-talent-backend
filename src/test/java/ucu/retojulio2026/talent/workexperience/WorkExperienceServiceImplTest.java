package ucu.retojulio2026.talent.workexperience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.workexperience.dto.CreateWorkExperienceRequest;
import ucu.retojulio2026.talent.workexperience.dto.UpdateWorkExperienceRequest;
import ucu.retojulio2026.talent.workexperience.dto.WorkExperienceMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkExperienceServiceImplTest {

    @Mock
    private WorkExperienceRepository workExperienceRepository;

    @Mock
    private WorkExperienceMapper workExperienceMapper;

    @Mock
    private StudentProfileService studentProfileService;

    private WorkExperienceServiceImpl workExperienceService;

    @BeforeEach
    void setUp() {
        workExperienceService = new WorkExperienceServiceImpl(
                workExperienceRepository,
                workExperienceMapper,
                studentProfileService
        );
    }

    @Test
    void crear_valida_la_fecha_de_fin_y_acepta_fin_null() {
        CreateWorkExperienceRequest invalidRequest =
                new CreateWorkExperienceRequest(
                        "student-1",
                        "Acme S.A.",
                        "Backend Developer",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2023, 1, 1),
                        "Desarrollo de APIs REST"
                );

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> workExperienceService.create(invalidRequest)
        );

        verify(workExperienceMapper, never()).toEntity(any());
        verify(workExperienceRepository, never()).save(any());

        CreateWorkExperienceRequest validRequest =
                new CreateWorkExperienceRequest(
                        "student-1",
                        "Acme S.A.",
                        "Backend Developer",
                        LocalDate.of(2022, 1, 1),
                        null,
                        "Desarrollo de APIs REST"
                );

        WorkExperience workExperience = new WorkExperience();
        workExperience.setStudentProfileId("student-1");
        workExperience.setEndDate(null);

        when(workExperienceMapper.toEntity(validRequest))
                .thenReturn(workExperience);

        when(workExperienceRepository.save(workExperience))
                .thenReturn(workExperience);

        WorkExperience result =
                workExperienceService.create(validRequest);

        assertSame(workExperience, result);
        assertNull(result.getEndDate());

        verify(workExperienceMapper).toEntity(validRequest);
        verify(workExperienceRepository).save(workExperience);
    }

    @Test
    void crear_falla_si_la_fecha_de_fin_es_anterior_a_la_fecha_de_inicio() {
        CreateWorkExperienceRequest request =
                new CreateWorkExperienceRequest(
                        "student-1",
                        "Acme S.A.",
                        "Backend Developer",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2023, 1, 1),
                        "Desarrollo de APIs REST"
                );

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> workExperienceService.create(request)
        );

        verify(workExperienceMapper, never()).toEntity(any());
        verify(workExperienceRepository, never()).save(any());
    }

    @Test
    void crear_falla_si_la_fecha_de_inicio_es_anterior_a_2015() {
        CreateWorkExperienceRequest request =
                new CreateWorkExperienceRequest(
                        "student-1",
                        "Acme S.A.",
                        "Backend Developer",
                        LocalDate.of(2014, 12, 31),
                        null,
                        "Desarrollo de APIs REST"
                );

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> workExperienceService.create(request)
        );

        verify(workExperienceMapper, never()).toEntity(any());
        verify(workExperienceRepository, never()).save(any());
    }

    @Test
    void crear_guarda_la_experiencia_con_el_id_nulo() {
        CreateWorkExperienceRequest request =
                new CreateWorkExperienceRequest(
                        "student-1",
                        "Acme S.A.",
                        "Backend Developer",
                        LocalDate.of(2022, 1, 1),
                        null,
                        "Desarrollo de APIs REST"
                );

        WorkExperience workExperience = new WorkExperience();
        workExperience.setWorkExperienceId("id-asignado");
        workExperience.setStudentProfileId("student-1");

        when(studentProfileService.existsById("student-1"))
                .thenReturn(true);

        when(workExperienceMapper.toEntity(request))
                .thenReturn(workExperience);

        when(workExperienceRepository.save(workExperience))
                .thenReturn(workExperience);

        WorkExperience result =
                workExperienceService.create(request);

        assertSame(workExperience, result);
        assertNull(result.getWorkExperienceId());

        verify(workExperienceMapper).toEntity(request);
        verify(workExperienceRepository).save(workExperience);
    }
}