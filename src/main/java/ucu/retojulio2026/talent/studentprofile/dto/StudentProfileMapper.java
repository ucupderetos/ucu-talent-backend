package ucu.retojulio2026.talent.studentprofile.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.studentprofile.StudentProfile;

//Mapea la entidad StudentProfile a sus DTOs y viceversa usando MapStruct.
@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    // studentProfileId lo genera la entidad (@PrePersist), no viene en el request.
    @Mapping(target = "studentProfileId", ignore = true)
    StudentProfile toEntity(CreateStudentProfileRequest request);

    // studentProfileId, userId y skills tienen el mismo nombre en la entidad y el response - mapeo automatico.
    StudentProfileResponse toResponse(StudentProfile studentProfile);
}
