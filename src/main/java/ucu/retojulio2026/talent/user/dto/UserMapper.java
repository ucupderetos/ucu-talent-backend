package ucu.retojulio2026.talent.user.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.user.User;

//Mapea la entidad User a  sus DTOs  y viceversa usando MapStruct.
//No es adecuado ni necesario exponer todos los datos de la base de
//datos, por eso se utilizan DTOs. Ademas tambien sirven para tomar los
//Json de entradas como Clases (Records) de Java.

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User user);
}
