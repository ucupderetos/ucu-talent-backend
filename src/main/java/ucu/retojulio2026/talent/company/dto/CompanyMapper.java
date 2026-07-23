package ucu.retojulio2026.talent.company.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.user.AccountStatus;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    // El id sale del JWT en el controller, nunca del body -- ver CompanyController.create().
    @Mapping(target = "companyId", source = "id")
    Company toEntity(String id, CreateCompanyRequest request);

    // status vive en User, no en Company (PK compartida) - se pasa aparte.
    @Mapping(target = "status", source = "status")
    CompanyResponse toResponse(Company company, AccountStatus status);
}
