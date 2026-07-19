package ucu.retojulio2026.talent.company;

public interface CompanyDeletionService {

    // Borra la Company y, en cascada, todo lo que depende de ella (Vacancy -> Vacancy_Application).
    void delete(String companyId);
}
