package ucu.retojulio2026.talent.company.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.company.Company;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "companyId", source = "userId")
    Company toEntity(CreateCompanyRequest request);

    CompanyResponse toResponse(Company company);
}
