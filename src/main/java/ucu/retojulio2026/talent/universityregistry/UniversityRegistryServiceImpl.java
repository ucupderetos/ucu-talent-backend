package ucu.retojulio2026.talent.universityregistry;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.common.DocumentNormalizer;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.universityregistry.dto.CreateUniversityRegistryRequest;
import ucu.retojulio2026.talent.universityregistry.dto.UniversityRegistryMapper;
import ucu.retojulio2026.talent.universityregistry.dto.UpdateUniversityRegistryRequest;

import java.util.List;

@Service
public class UniversityRegistryServiceImpl implements UniversityRegistryService {

    private final UniversityRegistryRepository universityRegistryRepository;
    private final UniversityRegistryMapper universityRegistryMapper;

    public UniversityRegistryServiceImpl(UniversityRegistryRepository universityRegistryRepository,
                                          UniversityRegistryMapper universityRegistryMapper) {
        this.universityRegistryRepository = universityRegistryRepository;
        this.universityRegistryMapper = universityRegistryMapper;
    }

    @Override
    public UniversityRegistry create(CreateUniversityRegistryRequest request) {
        UniversityRegistry universityRegistry = universityRegistryMapper.toEntity(request);
        universityRegistry.setDocumentNumber(DocumentNormalizer.normalize(request.documentNumber()));
        return universityRegistryRepository.save(universityRegistry);
    }

    @Override
    public UniversityRegistry getById(String id) {
        return universityRegistryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UniversityRegistry con id '" + id + "' no encontrado"));
    }

    @Override
    public List<UniversityRegistry> getAll() {
        return universityRegistryRepository.findAll();
    }

    @Override
    public UniversityRegistry update(String id, UpdateUniversityRegistryRequest request) {
        UniversityRegistry universityRegistry = getById(id);
        universityRegistry.setDocumentType(request.documentType());
        universityRegistry.setDocumentNumber(DocumentNormalizer.normalize(request.documentNumber()));
        universityRegistry.setName(request.name());
        universityRegistry.setSurname(request.surname());
        return universityRegistryRepository.save(universityRegistry);
    }

    @Override
    public void delete(String id) {
        if (!universityRegistryRepository.existsById(id)) {
            throw new ResourceNotFoundException("UniversityRegistry con id '" + id + "' no encontrado");
        }
        universityRegistryRepository.deleteById(id);
    }
}
