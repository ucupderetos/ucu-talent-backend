package ucu.retojulio2026.talent.admin;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.admin.dto.AdminMapper;
import ucu.retojulio2026.talent.admin.dto.CreateAdminRequest;
import ucu.retojulio2026.talent.admin.dto.UpdateAdminRequest;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.user.UserService;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final UserService userService;

    public AdminServiceImpl(AdminRepository adminRepository, AdminMapper adminMapper, UserService userService) {
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
        this.userService = userService;
    }

    @Override
    public Admin create(String id, CreateAdminRequest request) {
        if (!userService.existsById(id)) {
            throw new ResourceNotFoundException("User con id '" + id + "' no encontrado");
        }
        if (adminRepository.existsById(id)) {
            throw new DuplicateResourceException("El usuario '" + id + "' ya tiene un admin asociado");
        }
        Admin admin = adminMapper.toEntity(id, request);
        return adminRepository.save(admin);
    }

    @Override
    public Admin getById(String id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin con id '" + id + "' no encontrado"));
    }

    @Override
    public List<Admin> getAll() {
        return adminRepository.findAll();
    }

    @Override
    public Admin update(String id, UpdateAdminRequest request) {
        Admin admin = getById(id);
        admin.setName(request.name());
        admin.setSurname(request.surname());
        return adminRepository.save(admin);
    }

    @Override
    public void delete(String id) {
        if (!adminRepository.existsById(id)) {
            throw new ResourceNotFoundException("Admin con id '" + id + "' no encontrado");
        }
        adminRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return adminRepository.existsById(id);
    }
}
