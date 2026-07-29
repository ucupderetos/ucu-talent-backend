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
import ucu.retojulio2026.talent.user.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
}
