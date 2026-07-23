package ucu.retojulio2026.talent.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ucu.retojulio2026.talent.audit.dto.AuditLogResponse;
import ucu.retojulio2026.talent.audit.dto.AuditMapper;

@RestController
@RequestMapping("/audit")
@Tag(name = "Auditoria", description = "Solo lectura. ADMIN unicamente.")
public class AuditController {

    private final AuditMapper auditMapper;
    private final AuditService auditService;

    public AuditController(AuditMapper auditMapper, AuditService auditService) {
        this.auditMapper = auditMapper;
        this.auditService = auditService;
    }

    @Operation(summary = "Ver los registros de auditoria, paginados")
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getRecent(
            @Parameter(description = "Numero de pagina (0-indexed)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de pagina") @RequestParam(required = false, defaultValue = "20") int size) {

        Page<AuditLogResponse> response = auditService.getRecent(PageRequest.of(page, size))
                .map(auditMapper::toResponse);
        return ResponseEntity.ok(response);
    }
}