package ucu.retojulio2026.talent.vacancy.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ucu.retojulio2026.talent.company.dto.CompanyPublicResponse;
import ucu.retojulio2026.talent.vacancy.Vacancy;

@Mapper(componentModel = "spring")
public interface VacancyMapper {

    @Mapping(target = "vacancyId", ignore = true)
    Vacancy toEntity(CreateVacancyRequest request);

    VacancyResponse toResponse(Vacancy vacancy);
    VacancyStudentResponse toStudentResponse(Vacancy vacancy);
    VacancyManagementResponse toManagementResponse(VacancyManagementRow row);

    @Mapping(target = ".", source = "company")
    @Mapping(target = "status", source = "companyStatus")
    CompanyPublicResponse toCompanyPublicResponse(ResolvedVacancyRow row);

    @Mapping(target = "company", source = "row")
    ResolvedVacancyResponse toResolvedResponse(ResolvedVacancyRow row);
}