package ucu.retojulio2026.talent.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ucu.retojulio2026.talent.user.Role;

/**
 * Firma en tipos "planos" (String/enum) a proposito, no recibe la entidad
 * User: el guardado es asincrono (@Async) y correr en otro hilo con una
 * entidad JPA "viva" de la transaccion original es pedir problemas. El
 * caller (AuditAspect) ya extrajo del actor lo que necesita antes de
 * cruzar a otro hilo.
 */
public interface AuditService {

    void saveAuditLog(String traceId, String actorUserId, String actorEmail, Role actorRole,
                       String module, String action, String entityId,
                       String outcome, String message, String detail);
    Page<AuditLog> getRecent(Pageable pageable);
}
