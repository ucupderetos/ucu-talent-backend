package ucu.retojulio2026.talent.education.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ucu.retojulio2026.talent.education.Education;

@Mapper(componentModel = "spring")
public interface EducationMapper {

    @Mapping(target = "educationId", ignore = true)
    Education toEntity(CreateEducationRequest request);

    EducationResponse toResponse(Education education);
}
