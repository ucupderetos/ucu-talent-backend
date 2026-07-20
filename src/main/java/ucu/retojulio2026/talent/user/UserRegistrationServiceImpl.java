package ucu.retojulio2026.talent.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.user.dto.CreateUserRequest;


@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final CompanyService companyService;

    public UserRegistrationServiceImpl(UserService userService, StudentProfileService studentProfileService,
            CompanyService companyService) {
        this.userService = userService;
        this.studentProfileService = studentProfileService;
        this.companyService = companyService;
    }

    @Override
    @Transactional
    public User register(CreateUserRequest request) {
        User user = userService.create(request);

        switch (user.getRole()) {
            case ALUMNO -> studentProfileService.create(new CreateStudentProfileRequest(user.getUserId(), List.of()));
            case EMPRESA -> companyService.create(
                    new CreateCompanyRequest(user.getUserId(), null, null, null, null, null));
            case ADMIN -> {
                // ADMIN no tiene perfil asociado.
            }
        }

        return user;
    }
}
