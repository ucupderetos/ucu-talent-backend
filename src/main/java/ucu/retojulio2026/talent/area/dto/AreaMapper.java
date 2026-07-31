package ucu.retojulio2026.talent.area.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.area.Area;

@Mapper(componentModel = "spring")
public interface AreaMapper {

    @Mapping(target = "areaId", ignore = true)
    Area toEntity(CreateAreaRequest request);

    AreaResponse toResponse(Area area);
}
