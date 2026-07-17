package ucu.retojulio2026.talent.company.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.company.Company;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "approved", ignore = true)
    Company toEntity(CreateCompanyRequest request);

    CompanyResponse toResponse(Company company);
}
