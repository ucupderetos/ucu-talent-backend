package ucu.retojulio2026.talent.user;

public interface UserDeletionService {

    // Borra el User y, en cascada, todo lo que depende de su perfil asociado
    // (StudentProfile -> Education/WorkExperience/VacancyApplication, o Company -> Vacancy/VacancyApplication).
    void delete(String userId);
}
