package ucu.retojulio2026.talent.company;

public interface CompanyDeletionService {

    // Borra la Company y, en cascada
    void delete(String companyId);
}
