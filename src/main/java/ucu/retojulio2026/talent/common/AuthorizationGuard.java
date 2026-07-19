package ucu.retojulio2026.talent.common;

import org.springframework.security.oauth2.jwt.Jwt;


public final class AuthorizationGuard {

    private AuthorizationGuard() {
    }

    public static void requireOwnership(Jwt jwt, String ownerId) {
        if (!jwt.getSubject().equals(ownerId)) {
            throw new ForbiddenOperationException("Usuario autenticado no tiene permisos para modificar esta recurso.");
        }
    }
}
