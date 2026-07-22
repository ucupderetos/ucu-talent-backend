package ucu.retojulio2026.talent.vacancyapplication.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;

@Mapper(componentModel = "spring")
public interface VacancyApplicationMapper {

    @Mapping(target = "vacancyApplicationId", ignore = true)
    VacancyApplication toEntity(CreateVacancyApplicationRequest request);

    VacancyApplicationResponse toResponse(VacancyApplication vacancyApplication);

    @Mapping(target = "vacancyApplicationId", source = "application.vacancyApplicationId")
    @Mapping(target = "status", source = "application.status")
    @Mapping(target = "appliedAt", source = "application.appliedAt")
    @Mapping(target = "studentProfileId", source = "profile.studentProfileId")
    @Mapping(target = "name", source = "profile.name")
    @Mapping(target = "surname", source = "profile.surname")
    @Mapping(target = "documentType", source = "profile.documentType")
    @Mapping(target = "documentNumber", source = "profile.documentNumber")
    @Mapping(target = "phoneNumber", source = "profile.phoneNumber")
    @Mapping(target = "linkedinUrl", source = "profile.linkedinUrl")
    @Mapping(target = "skills", source = "profile.skills")
    VacancyApplicantResponse toApplicantResponse(VacancyApplication application, StudentProfile profile);
}
