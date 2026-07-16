package ucu.retojulio2026.talent.studentprofile;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.StudentProfileMapper;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.user.UserRepository;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentProfileMapper studentProfileMapper;
    private final UserRepository userRepository;

    public StudentProfileServiceImpl(StudentProfileRepository studentProfileRepository, StudentProfileMapper studentProfileMapper, UserRepository userRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.studentProfileMapper = studentProfileMapper;
        this.userRepository = userRepository;
    }

    @Override
    public StudentProfile create(CreateStudentProfileRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new ResourceNotFoundException("User con id '" + request.userId() + "' no encontrado");
        }
        StudentProfile studentProfile = studentProfileMapper.toEntity(request);
        return studentProfileRepository.save(studentProfile);
    }

    @Override
    public StudentProfile getById(String id) {
        return studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile con id '" + id + "' no encontrado"));
    }

    @Override
    public StudentProfile getByUserId(String userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile con userId '" + userId + "' no encontrado"));
    }

    @Override
    public void delete(String id) {
        if (!studentProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("StudentProfile con id '" + id + "' no encontrado");
        }
        studentProfileRepository.deleteById(id);
    }
}
