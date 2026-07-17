package ucu.retojulio2026.talent.degree.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ucu.retojulio2026.talent.degree.Degree;

//Mapea la entidad Degree a sus DTOs y viceversa usando MapStruct.
//No es adecuado ni necesario exponer todos los datos de la base de
//datos, por eso se utilizan DTOs. Ademas tambien sirven para tomar los
//Json de entradas como Clases (Records) de Java.
@Mapper(componentModel = "spring")
public interface DegreeMapper {

    @Mapping(target = "degreeId", ignore = true)
    Degree toEntity(CreateDegreeRequest request);

    // degreeId, areaId, name e isUcu tienen el mismo nombre en Degree y DegreeResponse - mapeo automatico.
    DegreeResponse toResponse(Degree degree);
}