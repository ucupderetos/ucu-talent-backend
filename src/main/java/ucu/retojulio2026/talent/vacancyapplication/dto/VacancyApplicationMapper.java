package ucu.retojulio2026.talent.vacancyapplication.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.vacancy.dto.VacancyMapper;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;

@Mapper(componentModel = "spring", uses = VacancyMapper.class)
public interface VacancyApplicationMapper {

    @Mapping(target = "vacancyApplicationId", ignore = true)
    VacancyApplication toEntity(CreateVacancyApplicationRequest request);

    VacancyApplicationResponse toResponse(VacancyApplication vacancyApplication);

    ApplicationListItemResponse toListItemResponse(ApplicationListItemRow row);

    @Mapping(target = "vacancyApplicationId", source = "application.vacancyApplicationId")
    @Mapping(target = "vacancyId", source = "application.vacancyId")
    @Mapping(target = "vacancyName", source = "vacancy.name")
    @Mapping(target = "companyId", source = "vacancy.companyId")
    @Mapping(target = "companyName", source = "companyName")
    @Mapping(target = "appliedAt", source = "application.appliedAt")
    @Mapping(target = "status", source = "application.status")
    @Mapping(target = "vacancyStatus", source = "vacancy.status")
    VacancyApplicationStudentResponse toStudentResponse(MyApplicationRow row);

    @Mapping(target = "application", source = "row")
    MyApplicationRowResponse toMyApplicationRowResponse(MyApplicationRow row);

}
