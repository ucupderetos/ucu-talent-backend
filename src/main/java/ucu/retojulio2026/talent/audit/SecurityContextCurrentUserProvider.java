package ucu.retojulio2026.talent.audit;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserRepository;

/**
 * Resuelve el usuario actual a partir del SecurityContext que arma
 * apiFilterChain (SecurityConfig) al validar el JWT.
 *
 * IMPORTANTE: JwtService.issue() firma el token con
 * .subject(user.getUserId()). Para un JwtAuthenticationToken,
 * Authentication.getName() devuelve ese subject, es decir, el userId.
 * Por eso aca se busca por id y no por email (a diferencia de un JWT
 * "tipico" donde el subject suele ser el username/email).
 *
 * Devuelve Optional.empty() en rutas de la publicFilterChain (login,
 * signup, swagger, /dev/**), donde no corre el filtro de JWT y no hay
 * Authentication. En esos casos AuditAspect no audita nada, que es lo
 * correcto: no hay quien auditar.
 */
@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    public SecurityContextCurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findById(authentication.getName());
    }
}
