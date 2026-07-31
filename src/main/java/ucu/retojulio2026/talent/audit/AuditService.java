package ucu.retojulio2026.talent.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ucu.retojulio2026.talent.user.Role;

public interface AuditService {

    void saveAuditLog(String traceId, String actorUserId, String actorEmail, Role actorRole,
                       String module, String action, String entityId,
                       String outcome, String message, String detail);
    Page<AuditLog> getRecent(Pageable pageable);
}
