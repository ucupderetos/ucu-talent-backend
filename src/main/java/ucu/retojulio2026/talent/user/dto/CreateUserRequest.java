package ucu.retojulio2026.talent.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ucu.retojulio2026.talent.common.validation.PublicSignupRole;
import ucu.retojulio2026.talent.common.DocumentType;
import ucu.retojulio2026.talent.user.Role;

//Datos que la API RECIBE para crear un usuario.
//Usar records garantiza inmutabilidad y evita boilerplate (getters, setters, equlas, etc)
public record CreateUserRequest(

        @Schema(description = "Nombre del usuario", example = "Nicolas")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Apellido del usuario", example = "Gonzalez")
        @NotBlank(message = "El apellido es obligatorio")
        String surname,

        @Schema(description = "Email unico del usuario", example = "nicogon@ucu.edu.uy")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @Schema(description = "Contraseña en texto plano (minimo 8 caracteres)", example = "unaClaveSegura123")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @Schema(description = "Rol del usuario en el registro publico. Solo ALUMNO o EMPRESA; ADMIN se crea por un flujo aparte.", example = "ALUMNO")
        @NotNull(message = "El rol es obligatorio")
        @PublicSignupRole
        Role role,

        @Schema(description = "Numero de telefono (opcional)", example = "+59899123456")
        String phoneNumber,

        @Schema(description = "Tipo de documento", example = "CEDULA_IDENTIDAD")
        @NotNull(message = "El tipo de documento es obligatorio")
        DocumentType documentType,

        @Schema(description = "Numero de documento", example = "1.234.567-8")
        @NotBlank(message = "El numero de documento es obligatorio")
        String documentNumber,

        @Schema(description = "URL de LinkedIn (opcional)", example = "https://linkedin.com/in/nicolas-gonzalez")
        String linkedinUrl

) {}
