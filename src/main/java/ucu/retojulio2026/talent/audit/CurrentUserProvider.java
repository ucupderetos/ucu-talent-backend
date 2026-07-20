package ucu.retojulio2026.talent.audit;

import java.util.Optional;

import ucu.retojulio2026.talent.user.User;

/**
 * Resuelve, si existe, el usuario autenticado que esta ejecutando la
 * request actual. AuditAspect depende de esta abstraccion (y no
 * directamente de Spring Security) para poder testear el aspecto sin
 * tener que simular todo un SecurityContext.
 */
public interface CurrentUserProvider {

    Optional<User> getCurrentUser();
}
