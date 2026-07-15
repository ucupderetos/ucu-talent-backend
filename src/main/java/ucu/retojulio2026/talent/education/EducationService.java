package ucu.retojulio2026.talent.education;

import java.util.List;

public interface EducationService {

    Education create(Education education);

    Education getById(String id);

    List<Education> getByPerfilAlumnoId(String perfilAlumnoId);

    Education update(String id, Education education);

    void delete(String id);
}
