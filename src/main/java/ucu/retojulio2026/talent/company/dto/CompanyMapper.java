package ucu.retojulio2026.talent.company.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.company.Company;

//Mapea la entidad Company a sus DTOs y viceversa usando MapStruct.
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    // companyId lo genera la entidad (@PrePersist), no viene en el request.
    @Mapping(target = "companyId", ignore = true)
    Company toEntity(CreateCompanyRequest request);

    // companyId, userId, industry, description, webUrl y linkedinUrl tienen el mismo nombre en la entidad y el response - mapeo automatico.
    CompanyResponse toResponse(Company company);
}
