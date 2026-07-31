package ucu.retojulio2026.talent.degree.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ucu.retojulio2026.talent.degree.Degree;

@Mapper(componentModel = "spring")
public interface DegreeMapper {

    @Mapping(target = "degreeId", ignore = true)
    Degree toEntity(CreateDegreeRequest request);

    DegreeResponse toResponse(Degree degree);
}
