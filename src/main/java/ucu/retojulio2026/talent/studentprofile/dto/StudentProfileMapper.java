package ucu.retojulio2026.talent.studentprofile.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.User;

@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    @Mapping(target = "studentProfileId", source = "id")
    StudentProfile toEntity(String id, CreateStudentProfileRequest request);

    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "registeredAt", source = "user.registeredAt")
    StudentProfileResponse toResponse(StudentProfile studentProfile, User user);
}
