package ucu.retojulio2026.talent.studentprofile.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.user.AccountStatus;

//Mapea la entidad StudentProfile a sus DTOs y viceversa usando MapStruct.
@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    // PK compartida: studentProfileId siempre es el mismo valor que userId (no se genera aparte).
    @Mapping(target = "studentProfileId", source = "userId")
    StudentProfile toEntity(CreateStudentProfileRequest request);

    // status vive en User, no en StudentProfile (PK compartida) - se pasa aparte.
    @Mapping(target = "status", source = "status")
    StudentProfileResponse toResponse(StudentProfile studentProfile, AccountStatus status);
}
