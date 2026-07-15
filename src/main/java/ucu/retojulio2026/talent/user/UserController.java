package ucu.retojulio2026.talent.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UserMapper;
import ucu.retojulio2026.talent.user.dto.UserResponse;

//Bean que indica que  es un Controller cuyos returns se serializan directo a JSON
@RestController
// Da el path de la url a donde llamar al Endpoint, ej: localhost:8080/user/{id} para obtener un usuario
@RequestMapping("/user")
@Tag(name = "Usuarios", description = "Alta, consulta y baja de usuarios") // agrupa los endpoints en Swagger UI
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    //GetMapping indica que usa el verbo HTTP GET para obtener un recurso
    @Operation(summary = "Obtener un usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id")
    })

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(
            @Parameter(description = "Id del usuario (NanoID de 12 caracteres)") @PathVariable String id) {
        User user = userService.getById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
        //Devuelve un UserResponse (Json, sin passwordHash) mas código HTTP 200 (OK)
        //Si no existe lanza 404 NotFound en GlobalExceptionHandler
    }

    @Operation(summary = "Buscar un usuario por email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "400", description = "El email tiene un formato invalido"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese email")
    })
    @GetMapping(params = "email")
    public ResponseEntity<UserResponse> getByEmail(
            @Parameter(description = "Email exacto del usuario", example = "washi@ucu.edu.uy")
            @RequestParam
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "El email no tiene un formato valido")
            String email) {
        User user = userService.getByEmail(email);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    //PostMapping indica que usa el verbo HTTP POST para guardar un recurso.
    //@Valid dispara las validaciones del CreateUserRequest (@NotBlank, @Email, etc)
    @Operation(summary = "Crear un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)")
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(created));
        //Devuelve el UserResponse (sin passwordHash) mas código HTTP 201 (Created)
    }

    //Lo mismo que los otros con DELETE
    @Operation(summary = "Eliminar un usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado (sin contenido)"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del usuario (NanoID de 12 caracteres)") @PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
        //devuelve sólo el código 204 No Content -  No siempre hay que devolver un JSON al front
        //pero siempre un código HTTP para que sepan si salio bien (200s) o hubo algun fallo
        // (400s cliente) (500s servidor)
    }
}
