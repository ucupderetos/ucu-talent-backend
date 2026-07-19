package ucu.retojulio2026.talent.company.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.company.Company;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    // PK compartida: companyId siempre es el mismo valor que userId (no se genera aparte).
    @Mapping(target = "companyId", source = "userId")
    @Mapping(target = "approved", ignore = true)
    Company toEntity(CreateCompanyRequest request);

    CompanyResponse toResponse(Company company);
}
