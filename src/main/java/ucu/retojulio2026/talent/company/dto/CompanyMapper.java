package ucu.retojulio2026.talent.company.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.user.AccountStatus;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "companyId", source = "id")
    Company toEntity(String id, CreateCompanyRequest request);

    @Mapping(target = "status", source = "status")
    CompanyResponse toResponse(Company company, AccountStatus status);
}
