package ucu.retojulio2026.talent.workexperience.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ucu.retojulio2026.talent.workexperience.WorkExperience;


@Mapper(componentModel = "spring")
public interface WorkExperienceMapper {

    @Mapping(target = "workExperienceId", ignore = true)
    WorkExperience toEntity(CreateWorkExperienceRequest request);

    WorkExperienceResponse toResponse(WorkExperience workExperience);
}
