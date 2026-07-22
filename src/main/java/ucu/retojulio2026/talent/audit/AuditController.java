package ucu.retojulio2026.talent.audit;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solo para verificar en Swagger que AuditAspect esta escribiendo
 * filas, sin tener que entrar a psql/DBeaver a mano. Restringido a ADMIN en
 * SecurityConfig (ver matcher "/audit/**"). Si esto se vuelve algo que se
 * quiere dejar en produccion, conviene sumarle paginado real (Pageable) y
 * filtros (por module, por actor, por rango de fechas) en vez del
 * findTop50ByOrderByCreatedAtDesc fijo que tiene ahora.
 */
@RestController
@RequestMapping("/audit")
@Tag(name = "Auditoria", description = "Solo lectura, para verificar que se esta auditando. ADMIN unicamente.")
public class AuditController {

    private final AuditRepository auditRepository;

    public AuditController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Operation(summary = "TEMPORAL: ver los ultimos 50 registros de auditoria")
    @GetMapping
    public ResponseEntity<List<AuditLog>> getRecent() {
        return ResponseEntity.ok(auditRepository.findTop50ByOrderByCreatedAtDesc());
    }
}