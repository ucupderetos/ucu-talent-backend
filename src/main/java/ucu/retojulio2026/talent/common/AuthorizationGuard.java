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

    public static void requireOwnershipOrRoles(Jwt jwt, String ownerId, String... roles) {
        if (jwt.getSubject().equals(ownerId)) {
            return;
        }
        String role = jwt.getClaimAsString("role");
        for (String allowed : roles) {
            if (allowed.equals(role)) {
                return;
            }
        }
        throw new ForbiddenOperationException("Usuario autenticado no tiene permisos para acceder a este recurso.");
    }
}
