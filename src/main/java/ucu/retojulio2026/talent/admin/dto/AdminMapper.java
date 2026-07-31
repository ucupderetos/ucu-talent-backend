package ucu.retojulio2026.talent.admin.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.admin.Admin;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(target = "adminId", source = "id")
    Admin toEntity(String id, CreateAdminRequest request);

    AdminResponse toResponse(Admin admin);
}
