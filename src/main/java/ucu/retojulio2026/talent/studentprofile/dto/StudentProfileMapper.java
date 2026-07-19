package ucu.retojulio2026.talent.studentprofile.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.studentprofile.StudentProfile;

//Mapea la entidad StudentProfile a sus DTOs y viceversa usando MapStruct.
@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    // PK compartida: studentProfileId siempre es el mismo valor que userId (no se genera aparte).
    @Mapping(target = "studentProfileId", source = "userId")
    StudentProfile toEntity(CreateStudentProfileRequest request);

    // studentProfileId, userId y skills tienen el mismo nombre en la entidad y el response - mapeo automatico.
    StudentProfileResponse toResponse(StudentProfile studentProfile);
}
