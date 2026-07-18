package ucu.retojulio2026.talent.education.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ucu.retojulio2026.talent.education.Education;

//Mapea la entidad Education a sus DTOs y viceversa usando MapStruct.
//El id se ignora en toEntity: lo asigna el servidor, nunca el cliente.
@Mapper(componentModel = "spring")
public interface EducationMapper {

    @Mapping(target = "educationId", ignore = true)
    Education toEntity(CreateEducationRequest request);

    // Todos los campos coinciden en nombre entre Education y EducationResponse -> mapeo automatico.
    EducationResponse toResponse(Education education);
}
