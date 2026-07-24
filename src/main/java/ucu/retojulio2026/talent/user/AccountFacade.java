package ucu.retojulio2026.talent.user;


public interface AccountFacade {

    void deleteAccount(String userId);

    void reviewAccount(String userId, AccountStatus status, String adminComment);

    boolean hasProfile(String userId);
}
