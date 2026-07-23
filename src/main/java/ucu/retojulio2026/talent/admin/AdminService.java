package ucu.retojulio2026.talent.admin;

import ucu.retojulio2026.talent.admin.dto.CreateAdminRequest;
import ucu.retojulio2026.talent.admin.dto.UpdateAdminRequest;

import java.util.List;

public interface AdminService {

    Admin create(String id, CreateAdminRequest request);

    Admin getById(String id);

    List<Admin> getAll();

    Admin update(String id, UpdateAdminRequest request);

    void delete(String id);

    boolean existsById(String id);
}
