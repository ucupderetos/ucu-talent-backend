package ucu.retojulio2026.talent.audit;

import java.util.Optional;

import ucu.retojulio2026.talent.user.User;

public interface CurrentUserProvider {

    Optional<User> getCurrentUser();
}
