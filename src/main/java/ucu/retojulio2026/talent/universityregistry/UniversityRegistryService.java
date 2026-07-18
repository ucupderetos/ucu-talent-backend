package ucu.retojulio2026.talent.universityregistry;

import ucu.retojulio2026.talent.universityregistry.dto.CreateUniversityRegistryRequest;
import ucu.retojulio2026.talent.universityregistry.dto.UpdateUniversityRegistryRequest;

import java.util.List;

public interface UniversityRegistryService {

    UniversityRegistry create(CreateUniversityRegistryRequest request);

    UniversityRegistry getById(String id);

    List<UniversityRegistry> getAll();

    UniversityRegistry update(String id, UpdateUniversityRegistryRequest request);

    void delete(String id);
}
