package ucu.retojulio2026.talent.studentprofile;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ucu.retojulio2026.talent.storage.StorageService;
import ucu.retojulio2026.talent.storage.dto.StorageUploadResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.StudentProfileMapper;
import ucu.retojulio2026.talent.studentprofile.dto.UpdateStudentProfileRequest;
import ucu.retojulio2026.talent.common.DocumentNormalizer;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentProfileMapper studentProfileMapper;
    private final UserService userService;
    private final StorageService storageService;

    public StudentProfileServiceImpl(StudentProfileRepository studentProfileRepository,
                                     StudentProfileMapper studentProfileMapper, UserService userService, StorageService storageService) {
        this.studentProfileRepository = studentProfileRepository;
        this.studentProfileMapper = studentProfileMapper;
        this.userService = userService;
        this.storageService = storageService;
    }

    private static final Set<String> ALLOWED_CV_CONTENT_TYPES = Set.of("application/pdf");

    private void validateCvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El CV es obligatorio."
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CV_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permite PDF para el CV."
            );
        }
    }

    @Override
    public StudentProfile create(String id, CreateStudentProfileRequest request) {
        if (!userService.existsById(id)) {
            throw new ResourceNotFoundException("User con id '" + id + "' no encontrado");
        }
        if (studentProfileRepository.existsById(id)) {
            throw new DuplicateResourceException("El usuario '" + id + "' ya tiene un perfil de alumno asociado");
        }

        String normalizedDocumentNumber = DocumentNormalizer.normalize(request.documentNumber());
        if (studentProfileRepository.existsByDocumentTypeAndDocumentNumber(request.documentType(), normalizedDocumentNumber)) {
            throw new DuplicateResourceException("Ya existe un alumno con ese tipo y numero de documento");
        }
        StudentProfile studentProfile = studentProfileMapper.toEntity(id, request);
        studentProfile.setDocumentNumber(normalizedDocumentNumber);
        studentProfile.setSkills(normalizeSkills(studentProfile.getSkills()));
        return studentProfileRepository.save(studentProfile);
    }

    @Override
    public StudentProfile update(String id, UpdateStudentProfileRequest request) {
        StudentProfile studentProfile = getById(id);
        studentProfile.setPhoneNumber(request.phoneNumber());
        studentProfile.setLinkedinUrl(request.linkedinUrl());
        studentProfile.setSkills(normalizeSkills(request.skills()));
        studentProfile.setDescription(request.description());
        return studentProfileRepository.save(studentProfile);
    }

    private List<String> normalizeSkills(List<String> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(skill -> !skill.isBlank())
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public StudentProfile updateCvFile(String id, MultipartFile file) {
        validateCvFile(file);

        StudentProfile studentProfile = getById(id);

        String oldObjectName = studentProfile.getCvFile();

        StorageUploadResponse uploaded = storageService.upload(file, "student-profiles/cv");

        try {
            studentProfile.setCvFile(uploaded.objectName());
            StudentProfile saved = studentProfileRepository.save(studentProfile);

            if (oldObjectName != null && !oldObjectName.isBlank()) {
                storageService.delete(oldObjectName);
            }

            return saved;
        } catch (RuntimeException e) {
            storageService.delete(uploaded.objectName());
            throw e;
        }
    }

    @Override
    public String getCvFile(String cvFile, Jwt jwt) {
        if (!studentProfileRepository.existsByCvFile(cvFile)) {
            throw new ResourceNotFoundException(
                    "StudentProfile con el CV '" + cvFile + "' no encontrado"
            );
        }

        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no tiene expiración."
            );
        }

        Duration remaining = Duration.between(Instant.now(), expiresAt);
        if (remaining.isNegative() || remaining.isZero()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token ya expiró."
            );
        }

        return storageService.getSignedUrl(cvFile, remaining).toString();
    }

    @Override
    public StudentProfile getById(String id) {
        return studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile con id '" + id + "' no encontrado"));
    }

    @Override
    public List<StudentProfile> getAll(AccountStatus status) {
        if (status == null) {
            return studentProfileRepository.findAll();
        }
        List<String> studentProfileIds = userService.getAll(status, Role.ALUMNO, Pageable.unpaged())
                .getContent()
                .stream()
                .map(User::getUserId)
                .toList();
        return studentProfileRepository.findAllById(studentProfileIds);
    }

    @Override
    public void delete(String id) {
        if (!studentProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("StudentProfile con id '" + id + "' no encontrado");
        }
        studentProfileRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteCvFile(String id) {
        StudentProfile studentProfile = getById(id);

        String oldObjectName = studentProfile.getCvFile();
        if (oldObjectName == null || oldObjectName.isBlank()) {
            throw new ResourceNotFoundException("El perfil no tiene CV cargado");
        }

        studentProfile.setCvFile(null);
        studentProfileRepository.save(studentProfile);
        storageService.delete(oldObjectName);
    }

    @Override
    public boolean existsById(String id) {
        return studentProfileRepository.existsById(id);
    }

    @Override
    public Map<AccountStatus, Long> getStatusSummary() {
        return userService.countByRoleGroupedByStatus(Role.ALUMNO);
    }

    @Override
    public void review(String id, LocalDateTime reviewedAt, String adminComment) {
        StudentProfile studentProfile = getById(id);
        studentProfile.setReviewedAt(reviewedAt);
        studentProfile.setAdminComment(adminComment);
        studentProfileRepository.save(studentProfile);
    }

    @Override
    public boolean hasProfile(String id) {
        return studentProfileRepository.existsById(id);
    }



}
