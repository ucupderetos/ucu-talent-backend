package ucu.retojulio2026.talent.vacancyapplication.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;

@Mapper(componentModel = "spring")
public interface VacancyApplicationMapper {

    @Mapping(target = "vacancyApplicationId", ignore = true)
    VacancyApplication toEntity(CreateVacancyApplicationRequest request);

    VacancyApplicationResponse toResponse(VacancyApplication vacancyApplication);
}
