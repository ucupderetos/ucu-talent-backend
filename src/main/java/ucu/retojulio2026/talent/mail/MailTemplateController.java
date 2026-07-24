package ucu.retojulio2026.talent.mail;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucu.retojulio2026.talent.mail.dto.MailTemplateMapper;
import ucu.retojulio2026.talent.mail.dto.MailTemplateResponse;
import ucu.retojulio2026.talent.mail.dto.UpdateMailTemplateRequest;

@RestController
@RequestMapping("/mail-template")
@Tag(name = "Templates de mail", description = "Consulta y edicion de los templates de mail (solo ADMIN)")
public class MailTemplateController {

    private final MailTemplateService mailTemplateService;
    private final MailTemplateMapper mailTemplateMapper;

    public MailTemplateController(MailTemplateService mailTemplateService, MailTemplateMapper mailTemplateMapper) {
        this.mailTemplateService = mailTemplateService;
        this.mailTemplateMapper = mailTemplateMapper;
    }

    @Operation(summary = "Listar todos los templates de mail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<MailTemplateResponse>> getAll() {
        List<MailTemplateResponse> response = mailTemplateService.getAll()
                .stream()
                .map(mailTemplateMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un template de mail por codigo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe un template con ese codigo")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{code}")
    public ResponseEntity<MailTemplateResponse> getByCode(
            @Parameter(description = "Codigo del template") @PathVariable MailTemplateCode code) {
        MailTemplate mailTemplate = mailTemplateService.getByCode(code);
        return ResponseEntity.ok(mailTemplateMapper.toResponse(mailTemplate));
    }

    @Operation(summary = "Actualizar el subject/body de un template de mail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe un template con ese codigo")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{code}")
    public ResponseEntity<MailTemplateResponse> update(
            @Parameter(description = "Codigo del template") @PathVariable MailTemplateCode code,
            @Valid @RequestBody UpdateMailTemplateRequest request) {
        MailTemplate updated = mailTemplateService.updateByCode(code, request);
        return ResponseEntity.ok(mailTemplateMapper.toResponse(updated));
    }
}
