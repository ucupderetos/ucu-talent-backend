package ucu.retojulio2026.talent.area.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.area.Area;

// Convierte entre la entidad Area y sus DTOs usando MapStruct.
@Mapper(componentModel = "spring")
public interface AreaMapper {

    // areaId lo genera la entidad (@PrePersist), no viene en el request.
    @Mapping(target = "areaId", ignore = true)
    Area toEntity(CreateAreaRequest request);

    AreaResponse toResponse(Area area);
}