package ucu.retojulio2026.talent.company;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.company.dto.CompanyMapper;
import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.user.UserService;

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
}
