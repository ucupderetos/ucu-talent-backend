# Mail Templates Persistidos + Notificaciones de Postulación/Vacante Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persistir los templates de mail (subject+body editables por ADMIN) en la BD y agregar los mails de postulación VISTO, vacante cerrada y vacante con seleccionado.

**Architecture:** Entidad `MailTemplate` (código fijo por enum, sembrada por migración) + un renderer de placeholders `{{variable}}` + `MailServiceImpl` refactorizado para resolver subject/body vía template en vez de strings hardcodeadas. Dos triggers nuevos: `VacancyApplicationServiceImpl.update()` para VISTO, y un componente nuevo `VacancyFinalizationNotifier` invocado desde el cron de vacantes vencidas.

**Tech Stack:** Spring Boot, JPA/Hibernate, PostgreSQL, Flyway, Lombok. Sin MapStruct para `MailTemplateMapper` (mapeo manual, necesita lógica de placeholders por código que MapStruct no resuelve limpio).

## Global Constraints

- **Nunca ejecutar `git commit`** — cada task incluye el comando de commit como referencia para que el usuario lo corra él mismo. El ejecutor de este plan (agente o Claude) NO debe correrlo.
- **Sin testing automatizado** — está explícitamente fuera de alcance del proyecto (ver `.claude/CLAUDE.md`). La verificación de cada task es `./mvnw clean compile -q` (nunca compile incremental, da falsos positivos) más, al final, una verificación manual end-to-end.
- IDs de entidades: NanoId de 12 caracteres vía `NanoIdGenerator`, generado en `@PrePersist`, igual que el resto del repo.
- Todos los endpoints nuevos documentados con anotaciones Swagger (`@Operation`, `@ApiResponses`, `@Tag`) siguiendo el estilo existente (ver `DegreeController`).
- El código de los 4 templates es fijo (`MailTemplateCode` enum): `NEW_APPLICATION`, `APPLICATION_VISTO`, `VACANCY_CLOSED`, `VACANCY_SELECTED`. Sin create/delete por API.
- `VACANCY_CLOSED`/`VACANCY_SELECTED` se disparan **solo** desde el cron `finalizeExpiredVacancies()`. Cierre manual (empresa o admin) queda fuera de alcance ahora.

---

### Task 1: Entidad `MailTemplate`, enum de códigos, repositorio y migración

**Files:**
- Create: `src/main/java/ucu/retojulio2026/talent/mail/MailTemplateCode.java`
- Modify: `src/main/java/ucu/retojulio2026/talent/mail/MailTemplate.java` (reemplaza el POJO actual, sin referencias en el resto del código — confirmado con `grep -rn "MailTemplate\b" src/main`)
- Create: `src/main/java/ucu/retojulio2026/talent/mail/MailTemplateRepository.java`
- Create: `src/main/resources/db/migration/V31__create_mail_template_table.sql`

**Interfaces:**
- Produces: `MailTemplateCode` enum (4 constantes), entidad `MailTemplate` con getters/setters Lombok (`getMailTemplateId()`, `getCode()`, `getSubject()`, `getBody()`, y setters equivalentes), `MailTemplateRepository extends JpaRepository<MailTemplate, String>` con `Optional<MailTemplate> findByCode(MailTemplateCode code)`.

- [ ] **Step 1: Crear el enum `MailTemplateCode`**

```java
package ucu.retojulio2026.talent.mail;

public enum MailTemplateCode {
    NEW_APPLICATION,
    APPLICATION_VISTO,
    VACANCY_CLOSED,
    VACANCY_SELECTED
}
```

- [ ] **Step 2: Reemplazar `MailTemplate.java` por la entidad persistida**

```java
package ucu.retojulio2026.talent.mail;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "mail_template")
public class MailTemplate {

    @Id
    @Column(name = "mail_template_id", length = 12, updatable = false, nullable = false)
    private String mailTemplateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 30, unique = true)
    private MailTemplateCode code;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;

    @PrePersist
    protected void assignId() {
        if (this.mailTemplateId == null) {
            this.mailTemplateId = NanoIdGenerator.generate();
        }
    }
}
```

- [ ] **Step 3: Crear `MailTemplateRepository`**

```java
package ucu.retojulio2026.talent.mail;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, String> {

    Optional<MailTemplate> findByCode(MailTemplateCode code);
}
```

- [ ] **Step 4: Crear la migración `V31__create_mail_template_table.sql`**

```sql
CREATE TABLE mail_template (
    mail_template_id VARCHAR(12)  NOT NULL,
    code              VARCHAR(30)  NOT NULL,
    subject           VARCHAR(200) NOT NULL,
    body              TEXT         NOT NULL,

    CONSTRAINT pk_mail_template PRIMARY KEY (mail_template_id),
    CONSTRAINT uq_mail_template_code UNIQUE (code),
    CONSTRAINT ck_mail_template_code CHECK (code IN ('NEW_APPLICATION', 'APPLICATION_VISTO', 'VACANCY_CLOSED', 'VACANCY_SELECTED'))
);

INSERT INTO mail_template (mail_template_id, code, subject, body) VALUES
('mt0newappl01', 'NEW_APPLICATION', 'Nueva postulación recibida',
'Hola,

Recibiste una nueva postulación.
Postulante: {{applicantName}}
Puesto: {{vacancyName}}

Saludos,
Equipo Talent'),

('mt0visto0002', 'APPLICATION_VISTO', 'Tu postulación fue vista',
'Hola {{studentName}},

La empresa revisó tu postulación al puesto {{vacancyName}}.
Te avisaremos si hay novedades.

Saludos,
Equipo Talent'),

('mt0closed003', 'VACANCY_CLOSED', 'La oferta laboral ha finalizado',
'Hola {{studentName}},

La oferta laboral {{vacancyName}} ha finalizado.
Gracias por tu interés.

Saludos,
Equipo Talent'),

('mt0select004', 'VACANCY_SELECTED', '¡Has sido seleccionado!',
'Hola {{studentName}},

¡Felicitaciones! Has sido seleccionado para el puesto {{vacancyName}} en {{companyName}}.
La empresa se pondrá en contacto con vos.

Saludos,
Equipo Talent');
```

- [ ] **Step 5: Compilar**

Run: `./mvnw clean compile -q`
Expected: sin output (BUILD SUCCESS silencioso con `-q`). Si falla, corregir antes de seguir.

- [ ] **Step 6: Commit (comando de referencia — no ejecutar, lo corre el usuario)**

```bash
git add src/main/java/ucu/retojulio2026/talent/mail/MailTemplateCode.java \
        src/main/java/ucu/retojulio2026/talent/mail/MailTemplate.java \
        src/main/java/ucu/retojulio2026/talent/mail/MailTemplateRepository.java \
        src/main/resources/db/migration/V31__create_mail_template_table.sql
git commit -m "feat: persist mail templates as MailTemplate entity"
```

---

### Task 2: Renderer de placeholders

**Files:**
- Create: `src/main/java/ucu/retojulio2026/talent/mail/MailTemplateRenderer.java`

**Interfaces:**
- Produces: `MailTemplateRenderer.render(String text, Map<String, String> variables)` → `String`, estático, sin estado.

- [ ] **Step 1: Crear `MailTemplateRenderer`**

```java
package ucu.retojulio2026.talent.mail;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MailTemplateRenderer {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    private MailTemplateRenderer() {
    }

    public static String render(String text, Map<String, String> variables) {
        if (text == null) {
            return null;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = variables.getOrDefault(key, matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
```

- [ ] **Step 2: Compilar**

Run: `./mvnw clean compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Verificación manual rápida (sin test automatizado)**

Abrir una consola Java rápida no es necesario: el comportamiento se verifica indirectamente en el Task 9 (end-to-end). Si querés confirmarlo ahora mismo a mano, un placeholder sin resolver (ej. `{{typo}}` sin esa key en el map) debe quedar literal en el texto de salida — así queda documentado el contrato para cuando se pruebe en Task 9.

- [ ] **Step 4: Commit (comando de referencia — no ejecutar)**

```bash
git add src/main/java/ucu/retojulio2026/talent/mail/MailTemplateRenderer.java
git commit -m "feat: add placeholder renderer for mail templates"
```

---

### Task 3: DTOs y mapper de `MailTemplate`

**Files:**
- Create: `src/main/java/ucu/retojulio2026/talent/mail/dto/MailTemplateResponse.java`
- Create: `src/main/java/ucu/retojulio2026/talent/mail/dto/UpdateMailTemplateRequest.java`
- Create: `src/main/java/ucu/retojulio2026/talent/mail/dto/MailTemplateMapper.java`

**Interfaces:**
- Consumes: `MailTemplate` (Task 1) con `getMailTemplateId()`, `getCode()`, `getSubject()`, `getBody()`.
- Produces: `MailTemplateResponse(mailTemplateId, code, subject, body, placeholders)`, `UpdateMailTemplateRequest(subject, body)`, bean `MailTemplateMapper.toResponse(MailTemplate)` → `MailTemplateResponse`.

- [ ] **Step 1: Crear `MailTemplateResponse`**

```java
package ucu.retojulio2026.talent.mail.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.mail.MailTemplateCode;

public record MailTemplateResponse(

        @Schema(description = "Id del template (NanoID)", example = "AbC123xYz890")
        String mailTemplateId,

        @Schema(description = "Codigo del template", example = "APPLICATION_VISTO")
        MailTemplateCode code,

        @Schema(description = "Asunto del mail", example = "Tu postulación fue vista")
        String subject,

        @Schema(description = "Cuerpo del mail, admite placeholders {{variable}}")
        String body,

        @Schema(description = "Placeholders disponibles para este template", example = "[\"studentName\", \"vacancyName\"]")
        List<String> placeholders) {
}
```

- [ ] **Step 2: Crear `UpdateMailTemplateRequest`**

```java
package ucu.retojulio2026.talent.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMailTemplateRequest(

        @Schema(description = "Asunto del mail", example = "Tu postulación fue vista")
        @NotBlank(message = "El subject es obligatorio")
        @Size(max = 200, message = "El subject no puede superar los 200 caracteres")
        String subject,

        @Schema(description = "Cuerpo del mail, admite placeholders {{variable}}")
        @NotBlank(message = "El body es obligatorio")
        String body) {
}
```

- [ ] **Step 3: Crear `MailTemplateMapper` (mapeo manual, no MapStruct)**

`placeholders` no vive en la entidad — es información estática por código, documentada acá para que el `GET` la devuelva y el ADMIN sepa qué variables puede usar al editar. Por eso este mapper es una clase `@Component` a mano en vez de una interfaz MapStruct como en el resto del repo (MapStruct no resuelve bien un campo derivado de una lookup table estática).

```java
package ucu.retojulio2026.talent.mail.dto;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.mail.MailTemplate;
import ucu.retojulio2026.talent.mail.MailTemplateCode;

@Component
public class MailTemplateMapper {

    private static final Map<MailTemplateCode, List<String>> PLACEHOLDERS_BY_CODE = Map.of(
            MailTemplateCode.NEW_APPLICATION, List.of("applicantName", "vacancyName"),
            MailTemplateCode.APPLICATION_VISTO, List.of("studentName", "vacancyName"),
            MailTemplateCode.VACANCY_CLOSED, List.of("studentName", "vacancyName"),
            MailTemplateCode.VACANCY_SELECTED, List.of("studentName", "vacancyName", "companyName"));

    public MailTemplateResponse toResponse(MailTemplate mailTemplate) {
        List<String> placeholders = PLACEHOLDERS_BY_CODE.getOrDefault(mailTemplate.getCode(), List.of());
        return new MailTemplateResponse(
                mailTemplate.getMailTemplateId(),
                mailTemplate.getCode(),
                mailTemplate.getSubject(),
                mailTemplate.getBody(),
                placeholders);
    }
}
```

- [ ] **Step 4: Compilar**

Run: `./mvnw clean compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit (comando de referencia — no ejecutar)**

```bash
git add src/main/java/ucu/retojulio2026/talent/mail/dto/MailTemplateResponse.java \
        src/main/java/ucu/retojulio2026/talent/mail/dto/UpdateMailTemplateRequest.java \
        src/main/java/ucu/retojulio2026/talent/mail/dto/MailTemplateMapper.java
git commit -m "feat: add MailTemplate DTOs and mapper"
```

---

### Task 4: `MailTemplateService` + implementación

**Files:**
- Create: `src/main/java/ucu/retojulio2026/talent/mail/MailTemplateService.java`
- Create: `src/main/java/ucu/retojulio2026/talent/mail/MailTemplateServiceImpl.java`

**Interfaces:**
- Consumes: `MailTemplateRepository` (Task 1), `MailTemplateRenderer.render` (Task 2), `UpdateMailTemplateRequest` (Task 3), `ResourceNotFoundException` (existe en `ucu.retojulio2026.talent.common`, mismo patrón que `DegreeServiceImpl.getById`).
- Produces: `MailTemplateService` con `getAll()`, `getByCode(MailTemplateCode)`, `updateByCode(MailTemplateCode, UpdateMailTemplateRequest)`, `render(MailTemplateCode, Map<String,String>)` → `MailTemplateService.RenderedMail(subject, body)`. Este `render` es lo que va a consumir `MailServiceImpl` en el Task 6.

- [ ] **Step 1: Crear la interfaz `MailTemplateService`**

```java
package ucu.retojulio2026.talent.mail;

import java.util.List;
import java.util.Map;

import ucu.retojulio2026.talent.mail.dto.UpdateMailTemplateRequest;

public interface MailTemplateService {

    List<MailTemplate> getAll();

    MailTemplate getByCode(MailTemplateCode code);

    MailTemplate updateByCode(MailTemplateCode code, UpdateMailTemplateRequest request);

    RenderedMail render(MailTemplateCode code, Map<String, String> variables);

    record RenderedMail(String subject, String body) {
    }
}
```

- [ ] **Step 2: Crear `MailTemplateServiceImpl`**

```java
package ucu.retojulio2026.talent.mail;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.mail.dto.UpdateMailTemplateRequest;

@Service
public class MailTemplateServiceImpl implements MailTemplateService {

    private final MailTemplateRepository mailTemplateRepository;

    public MailTemplateServiceImpl(MailTemplateRepository mailTemplateRepository) {
        this.mailTemplateRepository = mailTemplateRepository;
    }

    @Override
    public List<MailTemplate> getAll() {
        return mailTemplateRepository.findAll();
    }

    @Override
    public MailTemplate getByCode(MailTemplateCode code) {
        return mailTemplateRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MailTemplate con code '" + code + "' no encontrado"));
    }

    @Override
    public MailTemplate updateByCode(MailTemplateCode code, UpdateMailTemplateRequest request) {
        MailTemplate mailTemplate = getByCode(code);
        mailTemplate.setSubject(request.subject());
        mailTemplate.setBody(request.body());
        return mailTemplateRepository.save(mailTemplate);
    }

    @Override
    public RenderedMail render(MailTemplateCode code, Map<String, String> variables) {
        MailTemplate mailTemplate = getByCode(code);
        String subject = MailTemplateRenderer.render(mailTemplate.getSubject(), variables);
        String body = MailTemplateRenderer.render(mailTemplate.getBody(), variables);
        return new RenderedMail(subject, body);
    }
}
```

- [ ] **Step 3: Compilar**

Run: `./mvnw clean compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit (comando de referencia — no ejecutar)**

```bash
git add src/main/java/ucu/retojulio2026/talent/mail/MailTemplateService.java \
        src/main/java/ucu/retojulio2026/talent/mail/MailTemplateServiceImpl.java
git commit -m "feat: add MailTemplateService with placeholder rendering"
```

---

### Task 5: `MailTemplateController` (ADMIN)

**Files:**
- Create: `src/main/java/ucu/retojulio2026/talent/mail/MailTemplateController.java`

**Interfaces:**
- Consumes: `MailTemplateService` (Task 4), `MailTemplateMapper` (Task 3).
- Produces: `GET /mail-template`, `GET /mail-template/{code}`, `PUT /mail-template/{code}` — los tres solo ADMIN.

- [ ] **Step 1: Crear `MailTemplateController`**

```java
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
```

- [ ] **Step 2: Compilar**

Run: `./mvnw clean compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Verificación manual — levantar la app y probar el CRUD por Swagger**

```bash
./mvnw spring-boot:run
```

Con un token de ADMIN (o vía Swagger UI en `http://localhost:8080/swagger-ui/index.html`):
- `GET /mail-template` → debe devolver los 4 templates sembrados por la migración, cada uno con su `placeholders`.
- `PUT /mail-template/APPLICATION_VISTO` con body `{"subject": "test", "body": "hola {{studentName}}"}` → 200, y el `GET` posterior refleja el cambio.
- `GET /mail-template/APPLICATION_VISTO` sin rol ADMIN → 403.

Volver a poner el subject/body original de `APPLICATION_VISTO` (el de la migración) antes de seguir, para no arrastrar el valor de prueba al resto de la verificación.

- [ ] **Step 4: Commit (comando de referencia — no ejecutar)**

```bash
git add src/main/java/ucu/retojulio2026/talent/mail/MailTemplateController.java
git commit -m "feat: add MailTemplate admin CRUD endpoints"
```

---

### Task 6: Refactor de `MailService`/`MailServiceImpl` para usar templates persistidos

**Files:**
- Modify: `src/main/java/ucu/retojulio2026/talent/mail/MailService.java`
- Modify: `src/main/java/ucu/retojulio2026/talent/mail/MailServiceImpl.java`

**Interfaces:**
- Consumes: `MailTemplateService.render(MailTemplateCode, Map<String,String>)` (Task 4).
- Produces: `MailService` con 4 métodos — `sendCompanyNewApplicationEmail(String companyEmail, String applicantName, String vacancyName)`, `sendApplicationVistoEmail(String applicantEmail, String studentName, String vacancyName)`, `sendVacancyClosedEmail(String applicantEmail, String studentName, String vacancyName)`, `sendVacancySelectedEmail(String applicantEmail, String studentName, String vacancyName, String companyName)`. **Se elimina** `sendApplicantStatusChangedEmail` (el genérico actual) — reemplazado por `sendApplicationVistoEmail`, más específico. Los Tasks 7 y 8 consumen estos 4 métodos.

- [ ] **Step 1: Reescribir `MailService.java`**

```java
package ucu.retojulio2026.talent.mail;

public interface MailService {

    void sendCompanyNewApplicationEmail(String companyEmail, String applicantName, String vacancyName);

    void sendApplicationVistoEmail(String applicantEmail, String studentName, String vacancyName);

    void sendVacancyClosedEmail(String applicantEmail, String studentName, String vacancyName);

    void sendVacancySelectedEmail(String applicantEmail, String studentName, String vacancyName, String companyName);
}
```

- [ ] **Step 2: Reescribir `MailServiceImpl.java`**

```java
package ucu.retojulio2026.talent.mail;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MailServiceImpl implements MailService {
    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final MailValidator mailValidator;
    private final MailTemplateService mailTemplateService;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public MailServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, MailValidator mailValidator,
                            MailTemplateService mailTemplateService) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailValidator = mailValidator;
        this.mailTemplateService = mailTemplateService;
    }

    @Async("taskExecutor")
    @Override
    public void sendCompanyNewApplicationEmail(String companyEmail, String applicantName, String vacancyName) {
        sendTemplated(MailTemplateCode.NEW_APPLICATION, companyEmail, Map.of(
                "applicantName", applicantName,
                "vacancyName", vacancyName));
    }

    @Async("taskExecutor")
    @Override
    public void sendApplicationVistoEmail(String applicantEmail, String studentName, String vacancyName) {
        sendTemplated(MailTemplateCode.APPLICATION_VISTO, applicantEmail, Map.of(
                "studentName", studentName,
                "vacancyName", vacancyName));
    }

    @Async("taskExecutor")
    @Override
    public void sendVacancyClosedEmail(String applicantEmail, String studentName, String vacancyName) {
        sendTemplated(MailTemplateCode.VACANCY_CLOSED, applicantEmail, Map.of(
                "studentName", studentName,
                "vacancyName", vacancyName));
    }

    @Async("taskExecutor")
    @Override
    public void sendVacancySelectedEmail(String applicantEmail, String studentName, String vacancyName, String companyName) {
        sendTemplated(MailTemplateCode.VACANCY_SELECTED, applicantEmail, Map.of(
                "studentName", studentName,
                "vacancyName", vacancyName,
                "companyName", companyName));
    }

    private void sendTemplated(MailTemplateCode code, String to, Map<String, String> variables) {
        if (mailSender == null) {
            log.warn("SMTP no configurado: se omite correo '{}' a {}", code, to);
            return;
        }

        String normalizedTo = mailValidator.normalize(to);
        mailValidator.validateOrThrow(normalizedTo);
        try {
            MailTemplateService.RenderedMail renderedMail = mailTemplateService.render(code, variables);
            SimpleMailMessage message = new SimpleMailMessage();
            setFromIfConfigured(message);
            message.setTo(normalizedTo);
            message.setSubject(renderedMail.subject());
            message.setText(renderedMail.body());
            mailSender.send(message);
            log.info("Correo '{}' enviado a {}", code, normalizedTo);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo '" + code + "'", e);
        }
    }

    private void setFromIfConfigured(SimpleMailMessage message) {
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
    }
}
```

- [ ] **Step 3: Compilar — este paso va a fallar hasta el Task 7**

Run: `./mvnw clean compile -q`
Expected: **FALLA** porque `VacancyApplicationServiceImpl.java:156` todavía llama a `mailService.sendApplicantStatusChangedEmail(...)`, que ya no existe en la interfaz. Es esperado — se resuelve en el Task 7, que es indivisible de este cambio (no tiene sentido dejar el proyecto sin compilar entre tasks, pero separar los archivos ayuda a revisar cada uno). Si estás ejecutando este plan task por task con revisión intermedia, anotá el fallo esperado y seguí directo al Task 7 antes de considerar este bloque terminado.

- [ ] **Step 4: Commit (comando de referencia — no ejecutar; recién después del Task 7, cuando vuelva a compilar)**

```bash
git add src/main/java/ucu/retojulio2026/talent/mail/MailService.java \
        src/main/java/ucu/retojulio2026/talent/mail/MailServiceImpl.java \
        src/main/java/ucu/retojulio2026/talent/vacancyapplication/VacancyApplicationServiceImpl.java
git commit -m "feat: send mails via persisted templates instead of hardcoded strings"
```

---

### Task 7: Disparar `APPLICATION_VISTO` solo en la transición a VISTO

**Files:**
- Modify: `src/main/java/ucu/retojulio2026/talent/vacancyapplication/VacancyApplicationServiceImpl.java:142-160` (método `update`)

**Interfaces:**
- Consumes: `MailService.sendApplicationVistoEmail(String applicantEmail, String studentName, String vacancyName)` (Task 6). `StudentProfileService.getById(String)` y `UserService.getById(String)` ya están inyectados en esta clase (se usan en `create()`), no hace falta agregar dependencias nuevas.

- [ ] **Step 1: Reemplazar el cuerpo de `update()`**

Buscar este bloque actual:

```java
    @Override
    public VacancyApplication update(String id, VacancyApplicationStatus status) {
        VacancyApplication vacancyApplication = getById(id);
        VacancyApplicationStatus previousStatus = vacancyApplication.getStatus();
        if (status.ordinal() < vacancyApplication.getStatus().ordinal()) {
            throw new InvalidStatusTransitionException(
                    "No se puede retroceder de '" + vacancyApplication.getStatus() + "' a '" + status + "'");
        }
        vacancyApplication.setStatus(status);
        VacancyApplication updated = vacancyApplicationRepository.save(vacancyApplication);

        if (previousStatus != status) {
            Vacancy vacancy = vacancyService.getVacancyById(vacancyApplication.getVacancyId());
            User applicantUser = userService.getById(vacancyApplication.getStudentProfileId());
            mailService.sendApplicantStatusChangedEmail(applicantUser.getEmail(), vacancy.getName(), status);
        }

        return updated;
    }
```

Reemplazarlo por:

```java
    @Override
    public VacancyApplication update(String id, VacancyApplicationStatus status) {
        VacancyApplication vacancyApplication = getById(id);
        VacancyApplicationStatus previousStatus = vacancyApplication.getStatus();
        if (status.ordinal() < vacancyApplication.getStatus().ordinal()) {
            throw new InvalidStatusTransitionException(
                    "No se puede retroceder de '" + vacancyApplication.getStatus() + "' a '" + status + "'");
        }
        vacancyApplication.setStatus(status);
        VacancyApplication updated = vacancyApplicationRepository.save(vacancyApplication);

        if (previousStatus != VacancyApplicationStatus.VISTO && status == VacancyApplicationStatus.VISTO) {
            Vacancy vacancy = vacancyService.getVacancyById(vacancyApplication.getVacancyId());
            User applicantUser = userService.getById(vacancyApplication.getStudentProfileId());
            StudentProfile applicant = studentProfileService.getById(vacancyApplication.getStudentProfileId());
            String applicantFullName = applicant.getName() + " " + applicant.getSurname();
            mailService.sendApplicationVistoEmail(applicantUser.getEmail(), applicantFullName, vacancy.getName());
        }

        return updated;
    }
```

No hace falta agregar imports: `StudentProfile`, `User` y `Vacancy` ya están importados en este archivo (se usan en `create()`).

- [ ] **Step 2: Compilar (ahora sí, junto con el Task 6, tiene que pasar)**

Run: `./mvnw clean compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Verificación manual**

Con la app levantada (`./mvnw spring-boot:run`) y usuarios de prueba ya creados (alumno aprobado + empresa aprobada + vacante publicada + una postulación creada):
1. Como empresa dueña de la vacante: `PUT /vacancy-application/{id}` con `{"status": "VISTO"}` → 200.
2. Revisar logs de la app: debe aparecer `Correo 'APPLICATION_VISTO' enviado a <email>` (o el warning de SMTP no configurado si no hay SMTP en el `.env` local — cualquiera de las dos confirma que se llamó al flujo correcto, no al genérico viejo).
3. Repetir el `PUT` con `{"status": "VISTO"}` de nuevo (mismo estado) → no debe generar un segundo log de envío (el `if` exige `previousStatus != VISTO`).
4. `PUT` a `{"status": "FINALIZADO"}` → no debe disparar ningún mail de `MailService` (el trigger es solo para VISTO).

- [ ] **Step 4: Commit — este es el punto real de commit para Tasks 6+7 juntos (comando de referencia, no ejecutar)**

```bash
git add src/main/java/ucu/retojulio2026/talent/mail/MailService.java \
        src/main/java/ucu/retojulio2026/talent/mail/MailServiceImpl.java \
        src/main/java/ucu/retojulio2026/talent/vacancyapplication/VacancyApplicationServiceImpl.java
git commit -m "feat: send APPLICATION_VISTO mail only on transition to VISTO"
```

---

### Task 8: `VacancyFinalizationNotifier` + wiring en el cron de vacantes vencidas

**Files:**
- Create: `src/main/java/ucu/retojulio2026/talent/vacancy/VacancyFinalizationNotifier.java`
- Modify: `src/main/java/ucu/retojulio2026/talent/vacancy/VacancyServiceImpl.java` (constructor + método `finalizeExpiredVacancies`)

**Interfaces:**
- Consumes: `VacancyApplicationRepository.findByVacancyId(String)` (ya existe), `StudentProfileService.getById`, `UserService.getById`, `CompanyService.getById`, `MailService.sendVacancySelectedEmail` / `sendVacancyClosedEmail` (Task 6), `VacancyApplication.isAccepted()` (getter Lombok del campo `boolean accepted`), `Vacancy.getVacancyId()/getCompanyId()/getName()`.
- Produces: `VacancyFinalizationNotifier.notifyApplicants(Vacancy vacancy)` — sin retorno, efecto secundario (manda mails).

- [ ] **Step 1: Crear `VacancyFinalizationNotifier`**

```java
package ucu.retojulio2026.talent.vacancy;

import java.util.List;

import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.mail.MailService;
import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationRepository;

@Component
public class VacancyFinalizationNotifier {

    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final StudentProfileService studentProfileService;
    private final UserService userService;
    private final CompanyService companyService;
    private final MailService mailService;

    public VacancyFinalizationNotifier(VacancyApplicationRepository vacancyApplicationRepository,
                                        StudentProfileService studentProfileService,
                                        UserService userService,
                                        CompanyService companyService,
                                        MailService mailService) {
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.studentProfileService = studentProfileService;
        this.userService = userService;
        this.companyService = companyService;
        this.mailService = mailService;
    }

    public void notifyApplicants(Vacancy vacancy) {
        List<VacancyApplication> applications = vacancyApplicationRepository.findByVacancyId(vacancy.getVacancyId());
        if (applications.isEmpty()) {
            return;
        }

        Company company = companyService.getById(vacancy.getCompanyId());

        for (VacancyApplication application : applications) {
            StudentProfile student = studentProfileService.getById(application.getStudentProfileId());
            User studentUser = userService.getById(application.getStudentProfileId());
            String studentFullName = student.getName() + " " + student.getSurname();

            if (application.isAccepted()) {
                mailService.sendVacancySelectedEmail(
                        studentUser.getEmail(), studentFullName, vacancy.getName(), company.getName());
            } else {
                mailService.sendVacancyClosedEmail(
                        studentUser.getEmail(), studentFullName, vacancy.getName());
            }
        }
    }
}
```

- [ ] **Step 2: Inyectar el notifier en `VacancyServiceImpl` y llamarlo desde el cron**

Buscar el constructor actual:

```java
    public VacancyServiceImpl(VacancyRepository vacancyRepository, VacancyMapper vacancyMapper, CompanyService companyService, AreaService areaService, UserService userService, VacancyApplicationRepository vacancyApplicationRepository, VacancyFilterResolverImpl vacancyFilterResolverImpl) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyMapper = vacancyMapper;
        this.companyService = companyService;
        this.areaService = areaService;
        this.userService = userService;
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.vacancyFilterResolverImpl = vacancyFilterResolverImpl;
    }
```

Reemplazarlo por (agrega el campo y el parámetro `vacancyFinalizationNotifier`):

```java
    private final VacancyFinalizationNotifier vacancyFinalizationNotifier;

    public VacancyServiceImpl(VacancyRepository vacancyRepository, VacancyMapper vacancyMapper, CompanyService companyService, AreaService areaService, UserService userService, VacancyApplicationRepository vacancyApplicationRepository, VacancyFilterResolverImpl vacancyFilterResolverImpl, VacancyFinalizationNotifier vacancyFinalizationNotifier) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyMapper = vacancyMapper;
        this.companyService = companyService;
        this.areaService = areaService;
        this.userService = userService;
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.vacancyFilterResolverImpl = vacancyFilterResolverImpl;
        this.vacancyFinalizationNotifier = vacancyFinalizationNotifier;
    }
```

(La declaración `private final VacancyFinalizationNotifier vacancyFinalizationNotifier;` va junto al resto de los campos `private final` al principio de la clase, no pegada al constructor — seguí el orden existente de los otros campos.)

Buscar el método del cron:

```java
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Montevideo")
    @Transactional
    public void finalizeExpiredVacancies() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Montevideo"));

        List<Vacancy> expired = vacancyRepository
                .findByStatusAndClosingDateLessThanEqual((VacancyStatus.PUBLICADO), today);

        for (Vacancy vacancy : expired) {
            vacancy.setStatus(VacancyStatus.FINALIZADO);
        }
    }
```

Reemplazarlo por:

```java
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Montevideo")
    @Transactional
    public void finalizeExpiredVacancies() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Montevideo"));

        List<Vacancy> expired = vacancyRepository
                .findByStatusAndClosingDateLessThanEqual((VacancyStatus.PUBLICADO), today);

        for (Vacancy vacancy : expired) {
            vacancy.setStatus(VacancyStatus.FINALIZADO);
            vacancyFinalizationNotifier.notifyApplicants(vacancy);
        }
    }
```

No hace falta import nuevo: `VacancyFinalizationNotifier` queda en el mismo paquete `ucu.retojulio2026.talent.vacancy`.

- [ ] **Step 3: Compilar**

Run: `./mvnw clean compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit (comando de referencia — no ejecutar)**

```bash
git add src/main/java/ucu/retojulio2026/talent/vacancy/VacancyFinalizationNotifier.java \
        src/main/java/ucu/retojulio2026/talent/vacancy/VacancyServiceImpl.java
git commit -m "feat: notify applicants (selected/closed) when a vacancy is auto-finalized"
```

---

### Task 9: Verificación manual end-to-end

**Files:** ninguno (solo verificación, sin cambios de código).

Sin testing automatizado (fuera de alcance del proyecto). Esta task confirma con la app real que las piezas de los Tasks 1-8 funcionan juntas.

- [ ] **Step 1: Build limpio completo**

Run: `./mvnw clean install -q`
Expected: BUILD SUCCESS (corre también los tests que ya existan en el repo, si hay alguno de otro módulo — no se agregan tests nuevos en este plan).

- [ ] **Step 2: Levantar la app**

```bash
./mvnw spring-boot:run
```

Revisar en el log de arranque que Flyway aplicó `V31__create_mail_template_table.sql` sin errores.

- [ ] **Step 3: Confirmar el seed de templates**

`GET /mail-template` (con token ADMIN) → 4 templates, códigos `NEW_APPLICATION`, `APPLICATION_VISTO`, `VACANCY_CLOSED`, `VACANCY_SELECTED`, cada uno con el `subject`/`body` sembrado por la migración y su lista de `placeholders`.

- [ ] **Step 4: Flujo de postulación → VISTO**

1. Crear una postulación (`POST /vacancy-application`) de un alumno aprobado a una vacante `PUBLICADO`.
2. Como empresa dueña: `PUT /vacancy-application/{id}` con `{"status": "VISTO"}`.
3. Log esperado: `Correo 'APPLICATION_VISTO' enviado a <email-del-alumno>` (o el warning de SMTP no configurado, si no hay SMTP real en el entorno local — igual confirma que se intentó el template correcto).

- [ ] **Step 5: Flujo de finalización de vacante (forzado, sin esperar al cron diario)**

Para no esperar a la medianoche:
1. Crear una vacante con `closingDate` en el pasado (o usar `PUT /vacancy/{id}` para llevar la `closingDate` a ayer, si el estado lo permite).
2. Crear dos postulaciones a esa vacante desde dos alumnos distintos; a una de ellas marcarla `PATCH /vacancy-application/{id}/accept`.
3. Invocar el método `finalizeExpiredVacancies()` manualmente. La forma más simple sin exponer un endpoint nuevo (fuera de alcance de este plan) es correr la app con el breakpoint/log activado y esperar al disparo real del cron, o ajustar transitoriamente la expresión cron a un valor cercano (ej. cada minuto) en el entorno local, probar, y devolverla a `"0 0 0 * * *"` antes de terminar — **no dejar ese cambio commiteado**.
4. Log esperado: un `Correo 'VACANCY_SELECTED' enviado a <email>` para el alumno aceptado y un `Correo 'VACANCY_CLOSED' enviado a <email>` para el otro.
5. `GET /vacancy/{id}` → `status: FINALIZADO`.

- [ ] **Step 6: Confirmar que el genérico viejo ya no existe**

`grep -rn "sendApplicantStatusChangedEmail" src/main` → sin resultados. Si aparece algo, hay un caller que quedó sin migrar al Task 7.

- [ ] **Step 7: Reportar al usuario**

Resumir qué se verificó y con qué resultado (logs vistos, endpoints probados). No marcar la feature como "lista"/"funcionando" sin haber corrido estos pasos con la app real — ver [[test-means-test-not-fix]].
