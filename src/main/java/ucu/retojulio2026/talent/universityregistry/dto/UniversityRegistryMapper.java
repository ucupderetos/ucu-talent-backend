package ucu.retojulio2026.talent.universityregistry.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ucu.retojulio2026.talent.universityregistry.UniversityRegistry;

@Mapper(componentModel = "spring")
public interface UniversityRegistryMapper {

    @Mapping(target = "id", ignore = true)
    UniversityRegistry toEntity(CreateUniversityRegistryRequest request);

    UniversityRegistryResponse toResponse(UniversityRegistry universityRegistry);
}
