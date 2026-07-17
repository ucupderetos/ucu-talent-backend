package ucu.retojulio2026.talent.vacancy.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ucu.retojulio2026.talent.vacancy.Vacancy;

@Mapper(componentModel = "spring")
public interface VacancyMapper {

    @Mapping(target = "vacancyId", ignore = true)
    //Luego agregar CompanyId y AreaId
    Vacancy toEntity(CreateVacancyRequest request);

    VacancyResponse toResponse(Vacancy vacancy);
}