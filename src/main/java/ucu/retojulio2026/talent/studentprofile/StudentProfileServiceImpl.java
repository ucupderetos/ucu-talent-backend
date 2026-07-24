package ucu.retojulio2026.talent.studentprofile;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.StudentProfileMapper;
import ucu.retojulio2026.talent.studentprofile.dto.UpdateStudentProfileRequest;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentProfileMapper studentProfileMapper;
    private final UserService userService;

    public StudentProfileServiceImpl(StudentProfileRepository studentProfileRepository,
                                     StudentProfileMapper studentProfileMapper, UserService userService) {
        this.studentProfileRepository = studentProfileRepository;
        this.studentProfileMapper = studentProfileMapper;
        this.userService = userService;
    }

    @Override
    public StudentProfile create(String id, CreateStudentProfileRequest request) {
        if (!userService.existsById(id)) {
            throw new ResourceNotFoundException("User con id '" + id + "' no encontrado");
        }
        if (studentProfileRepository.existsById(id)) {
            throw new DuplicateResourceException("El usuario '" + id + "' ya tiene un perfil de alumno asociado");
        }
        StudentProfile studentProfile = studentProfileMapper.toEntity(id, request);
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
    public StudentProfile getById(String id) {
        return studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile con id '" + id + "' no encontrado"));
    }

    @Override
    public List<StudentProfile> getAll() {
        return studentProfileRepository.findAll();
    }

    @Override
    public void delete(String id) {
        if (!studentProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("StudentProfile con id '" + id + "' no encontrado");
        }
        studentProfileRepository.deleteById(id);
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
