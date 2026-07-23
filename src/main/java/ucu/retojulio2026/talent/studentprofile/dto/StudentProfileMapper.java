package ucu.retojulio2026.talent.studentprofile.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.user.AccountStatus;

//Mapea la entidad StudentProfile a sus DTOs y viceversa usando MapStruct.
@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    // PK compartida: studentProfileId siempre es el mismo valor que userId (no se genera aparte).
    // El id sale del JWT en el controller, nunca del body -- ver StudentProfileController.create().
    @Mapping(target = "studentProfileId", source = "id")
    StudentProfile toEntity(String id, CreateStudentProfileRequest request);

    // status vive en User, no en StudentProfile (PK compartida) - se pasa aparte.
    @Mapping(target = "status", source = "status")
    StudentProfileResponse toResponse(StudentProfile studentProfile, AccountStatus status);
}
