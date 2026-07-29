package ucu.retojulio2026.talent.company;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.company.dto.CompanyMapper;
import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.UpdateCompanyRequest;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private UserService userService;

    private CreateCompanyRequest sampleRequest() {
        return new CreateCompanyRequest("ACME S.A.", "Tecnologia", "Consultora de software a medida",
                "https://acme.com", "https://linkedin.com/company/acme", Department.MONTEVIDEO);
    }

    @Test
    void alta_de_empresa_sin_usuario_lanza_resource_not_found_exception() {
        CompanyServiceImpl service = new CompanyServiceImpl(companyRepository, companyMapper, userService);
        when(userService.existsById("user-1")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create("user-1", sampleRequest()));

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void un_usuario_no_puede_tener_mas_de_una_empresa_asociada() {
        CompanyServiceImpl service = new CompanyServiceImpl(companyRepository, companyMapper, userService);
        when(userService.existsById("user-1")).thenReturn(true);
        when(companyRepository.existsById("user-1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create("user-1", sampleRequest()));

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void editar_empresa_actualiza_razon_social_industria_descripcion_web_linkedin_y_localidad() {
        CompanyServiceImpl service = new CompanyServiceImpl(companyRepository, companyMapper, userService);
        Company existing = new Company();
        existing.setCompanyId("company-1");
        UpdateCompanyRequest request = new UpdateCompanyRequest(
                "ACME S.A.", "Tecnologia", "Consultora de software a medida",
                "https://acme.com", "https://linkedin.com/company/acme", Department.MONTEVIDEO);
        when(companyRepository.findById("company-1")).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company updated = service.update("company-1", request);

        assertThat(updated.getName()).isEqualTo("ACME S.A.");
        assertThat(updated.getIndustry()).isEqualTo("Tecnologia");
        assertThat(updated.getDescription()).isEqualTo("Consultora de software a medida");
        assertThat(updated.getWebUrl()).isEqualTo("https://acme.com");
        assertThat(updated.getLinkedinUrl()).isEqualTo("https://linkedin.com/company/acme");
        assertThat(updated.getLocation()).isEqualTo(Department.MONTEVIDEO);
    }

    @Test
    void listado_con_status_resuelve_los_ids_via_user_service_y_pide_solo_esos() {
        CompanyServiceImpl service = new CompanyServiceImpl(companyRepository, companyMapper, userService);
        User companyUser = new User();
        companyUser.setUserId("company-1");
        Page<User> page = new PageImpl<>(List.of(companyUser));
        when(userService.getAll(AccountStatus.APROBADO, Role.EMPRESA, Pageable.unpaged())).thenReturn(page);
        when(companyRepository.findAllById(List.of("company-1"))).thenReturn(List.of(new Company()));

        service.getAll(AccountStatus.APROBADO);

        verify(userService).getAll(AccountStatus.APROBADO, Role.EMPRESA, Pageable.unpaged());
        verify(companyRepository).findAllById(List.of("company-1"));
        verify(companyRepository, never()).findAll();
    }

    @Test
    void listado_sin_status_devuelve_todas_las_empresas() {
        CompanyServiceImpl service = new CompanyServiceImpl(companyRepository, companyMapper, userService);

        service.getAll(null);

        verify(companyRepository).findAll();
        verify(userService, never()).getAll(any(), any(), any());
        verify(companyRepository, never()).findAllById(any());
    }

    @Test
    void registrar_la_revision_guarda_reviewed_at_y_admin_comment() {
        CompanyServiceImpl service = new CompanyServiceImpl(companyRepository, companyMapper, userService);
        Company existing = new Company();
        existing.setCompanyId("company-1");
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 7, 29, 10, 0);
        when(companyRepository.findById("company-1")).thenReturn(Optional.of(existing));

        service.review("company-1", reviewedAt, "Datos verificados");

        assertThat(existing.getReviewedAt()).isEqualTo(reviewedAt);
        assertThat(existing.getAdminComment()).isEqualTo("Datos verificados");
        verify(companyRepository, times(1)).save(existing);
    }
}
