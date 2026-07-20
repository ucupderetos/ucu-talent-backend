package ucu.retojulio2026.talent.admin.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.admin.Admin;

//Mapea la entidad Admin a sus DTOs y viceversa usando MapStruct.
@Mapper(componentModel = "spring")
public interface AdminMapper {

    // PK compartida: adminId siempre es el mismo valor que userId (no se genera aparte).
    @Mapping(target = "adminId", source = "userId")
    Admin toEntity(CreateAdminRequest request);

    // adminId, name y surname tienen el mismo nombre en la entidad y el response - mapeo automatico.
    AdminResponse toResponse(Admin admin);
}
