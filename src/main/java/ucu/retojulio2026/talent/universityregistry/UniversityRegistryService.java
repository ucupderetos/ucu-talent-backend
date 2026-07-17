package ucu.retojulio2026.talent.universityregistry;

import ucu.retojulio2026.talent.universityregistry.dto.CreateUniversityRegistryRequest;
import ucu.retojulio2026.talent.universityregistry.dto.UpdateUniversityRegistryRequest;

public interface UniversityRegistryService {

    UniversityRegistry create(CreateUniversityRegistryRequest request);

    UniversityRegistry getById(String id);

    UniversityRegistry update(String id, UpdateUniversityRegistryRequest request);

    void delete(String id);
}
