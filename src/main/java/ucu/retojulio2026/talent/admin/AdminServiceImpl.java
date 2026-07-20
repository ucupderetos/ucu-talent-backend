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
    public Admin create(CreateAdminRequest request) {
        if (!userService.existsById(request.userId())) {
            throw new ResourceNotFoundException("User con id '" + request.userId() + "' no encontrado");
        }
        // adminId es siempre igual a userId (PK compartida): si ya existe,
        // repository.save(...) haria un UPDATE silencioso (merge) en vez de fallar,
        // porque el id ya viene seteado.
        if (adminRepository.existsById(request.userId())) {
            throw new DuplicateResourceException("El usuario '" + request.userId() + "' ya tiene un admin asociado");
        }
        Admin admin = adminMapper.toEntity(request);
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
        Admin admin = getById(id); // lanza 404 si no existe
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
