package ucu.retojulio2026.talent.dev;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucu.retojulio2026.talent.admin.AdminService;
import ucu.retojulio2026.talent.admin.dto.CreateAdminRequest;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.user.dto.UserMapper;
import ucu.retojulio2026.talent.user.dto.UserResponse;


@RestController
@RequestMapping("/dev")
@Tag(name = "ADMIN (temporal)", description = "Para que puedan crear un ADMIN y probar cosas, es TEMPORAL!.")
public class DevAdminController {

    private final UserService userService;
    private final AdminService adminService;
    private final UserMapper userMapper;

    public DevAdminController(UserService userService, AdminService adminService,
            UserMapper userMapper) {
        this.userService = userService;
        this.adminService = adminService;
        this.userMapper = userMapper;
    }

    @Operation(summary = "TEMPORAL: crear un ADMIN (cuenta + perfil) para pruebas")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Admin creado, ya puede loguearse"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese email")
    })
    @PostMapping("/admin")
    @Transactional
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody CreateDevAdminRequest request) {
        User admin = userService.createAdmin(request.email(), request.password());
        adminService.create(admin.getUserId(), new CreateAdminRequest(request.name(), request.surname()));

        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(admin));
    }
}
