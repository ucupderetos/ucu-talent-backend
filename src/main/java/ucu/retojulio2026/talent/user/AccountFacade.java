package ucu.retojulio2026.talent.user;

// Punto de entrada unico para operaciones que cruzan User + su perfil (StudentProfile o
// Company). UserServiceImpl/StudentProfileServiceImpl/CompanyServiceImpl nunca se conocen
// entre si (evita el ciclo de dependencias); solo este Facade conoce a los tres.
public interface AccountFacade {

    void deleteAccount(String userId);

    void reviewAccount(String userId, AccountStatus status, String adminComment);
}
