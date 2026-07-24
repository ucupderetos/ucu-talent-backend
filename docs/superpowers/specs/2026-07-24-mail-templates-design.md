# Diseño: Templates de mail persistidos + notificaciones de postulación/vacante

## Contexto

Ya existe envío de mail (SMTP + `MailService`/`MailServiceImpl`) con dos casos hardcodeados:
- Nueva postulación → mail a la empresa.
- Cambio de estado de `VacancyApplication` (genérico, cualquier transición) → mail al alumno.

El body de estos mails vive como strings Java (`buildXxxBody`), no editable sin deployar.

`MailTemplate` ya está listado en el SRS (sección 7) como entidad del modelo de datos, así que persistirlo no es una desviación del MER aprobado.

## Objetivo

1. Los templates de mail (subject + body) pasan a vivir en la base de datos, editables por ADMIN sin tocar código.
2. Se agregan 3 casos de negocio nuevos, además del ya existente de nueva postulación:
   - Postulación pasa a estado `VISTO` → mail al alumno.
   - Vacante finaliza (por fecha, vía cron) y el alumno **no** fue aceptado → mail de cierre genérico.
   - Vacante finaliza (por fecha, vía cron) y el alumno **fue** aceptado (`accepted = true`) → mail de selección.

## Fuera de alcance (explícito)

- Disparar `VACANCY_CLOSED`/`VACANCY_SELECTED` desde el cierre manual de la empresa o desde el admin — queda para una iteración futura con patrón Observer. No se implementa ahora.
- Create/Delete de templates vía API — el set de códigos es fijo (enum), sembrado por migración.
- Testing automatizado, CI/CD (fuera de alcance general del proyecto, ver CLAUDE.md).

## Modelo de datos

### Entidad `MailTemplate` (paquete `mail`, reemplaza el POJO muerto `MailTemplate.java` actual sin referencias)

| Campo | Tipo | Notas |
|---|---|---|
| `mailTemplateId` | String(12) | PK, NanoId, igual que el resto del repo |
| `code` | enum `MailTemplateCode` (STRING) | único, not null |
| `subject` | String | not null |
| `body` | Text | not null, contiene placeholders `{{variable}}` |

### Enum `MailTemplateCode`

```
NEW_APPLICATION    // empresa recibe una nueva postulación
APPLICATION_VISTO   // postulación pasa a estado VISTO
VACANCY_CLOSED      // vacante finalizó, alumno no seleccionado
VACANCY_SELECTED    // vacante finalizó, alumno seleccionado (accepted=true)
```

Cada código tiene, en código (no en BD), la lista de placeholders que acepta — se expone solo como información de solo lectura en las respuestas de la API para que el admin sepa qué puede usar al editar.

| Código | Placeholders |
|---|---|
| `NEW_APPLICATION` | `applicantName`, `vacancyName` |
| `APPLICATION_VISTO` | `studentName`, `vacancyName` |
| `VACANCY_CLOSED` | `studentName`, `vacancyName` |
| `VACANCY_SELECTED` | `studentName`, `vacancyName`, `companyName` |

### Migración `V31__create_mail_template_table.sql`

Crea la tabla `mail_template` y siembra las 4 filas con contenido default en español, tono consistente con el mail de nueva postulación que ya existe hoy (ese texto se migra tal cual a `body`/`subject` del template `NEW_APPLICATION`).

## Renderizado de placeholders

Utilidad `MailTemplateRenderer` (paquete `mail`), método estático:

```java
String render(String text, Map<String, String> variables)
```

- Busca ocurrencias de `{{key}}` con regex (`\{\{(\w+)\}\}`).
- Si `key` está en `variables`, reemplaza por el valor.
- Si no está, deja el placeholder literal en el texto (para que un typo del admin sea visible en vez de desaparecer en silencio).
- Se aplica tanto a `subject` como a `body`.

## Servicio y API de `MailTemplate` (solo lectura/edición, ADMIN)

`MailTemplateRepository extends JpaRepository<MailTemplate, String>` + `findByCode(MailTemplateCode code)`.

`MailTemplateService`:
- `getAll()`
- `getByCode(MailTemplateCode code)` → 404 (`ResourceNotFoundException`) si no existe (no debería pasar, está sembrado por migración)
- `updateByCode(MailTemplateCode code, UpdateMailTemplateRequest request)` → actualiza `subject`/`body`, guarda
- `render(MailTemplateCode code, Map<String,String> variables)` → devuelve subject/body ya renderizados, usado internamente por `MailServiceImpl`

DTOs:
- `MailTemplateResponse(mailTemplateId, code, subject, body, placeholders: List<String>)`
- `UpdateMailTemplateRequest(subject: @NotBlank String, body: @NotBlank String)`

`MailTemplateController` (`/mail-template`), `@PreAuthorize("hasRole('ADMIN')")` a nivel de clase:
- `GET /mail-template` — lista los 4
- `GET /mail-template/{code}` — uno por código
- `PUT /mail-template/{code}` — edita subject/body

## `MailService` — cambios

Mismo shape de métodos públicos que hoy (para no romper los call sites más de lo necesario), pero la implementación ahora resuelve el contenido vía template en BD:

```java
void sendCompanyNewApplicationEmail(String companyEmail, String applicantName, String vacancyName); // ya existe, ahora usa MailTemplateCode.NEW_APPLICATION
void sendApplicationVistoEmail(String applicantEmail, String studentName, String vacancyName);        // nuevo, MailTemplateCode.APPLICATION_VISTO
void sendVacancyClosedEmail(String applicantEmail, String studentName, String vacancyName);           // nuevo, MailTemplateCode.VACANCY_CLOSED
void sendVacancySelectedEmail(String applicantEmail, String studentName, String vacancyName, String companyName); // nuevo, MailTemplateCode.VACANCY_SELECTED
```

`MailServiceImpl` gana un método privado común:

```java
private void sendTemplated(MailTemplateCode code, String to, Map<String, String> variables)
```

que hace lo mismo que los métodos actuales (chequeo de `mailSender == null`, normalizar/validar email, `@Async`, log, `RuntimeException` en catch) pero resolviendo subject/body vía `mailTemplateService.render(code, variables)` en vez de los métodos `buildXxxBody` hardcodeados (que se eliminan).

`sendApplicantStatusChangedEmail` (el genérico actual que dispara en cualquier transición de estado) **se elimina** del `MailService` — se reemplaza por `sendApplicationVistoEmail`, que es más específico.

## Triggers

### `APPLICATION_VISTO`

En `VacancyApplicationServiceImpl.update(String id, VacancyApplicationStatus status)`: hoy dispara el mail genérico si `previousStatus != status`. Se acota a:

```java
if (previousStatus != VacancyApplicationStatus.VISTO && status == VacancyApplicationStatus.VISTO) {
    // buscar nombre del alumno (studentProfileService.getById) + nombre de vacante
    mailService.sendApplicationVistoEmail(applicantUser.getEmail(), studentFullName, vacancy.getName());
}
```

La autorización (solo la empresa dueña de la vacante puede pegarle a este endpoint) ya está enforced en `VacancyApplicationController.update()` vía `AuthorizationGuard.requireOwnership`, no cambia.

### `VACANCY_CLOSED` / `VACANCY_SELECTED`

Nuevo componente `VacancyFinalizationNotifier` (paquete `vacancy`, junto al cron que lo invoca), responsabilidad única: dado un `Vacancy` recién finalizado, notificar a todos sus postulantes.

Se invoca desde `VacancyServiceImpl.finalizeExpiredVacancies()` (el cron diario), una vez por cada vacante vencida, **después** de marcarla `FINALIZADO`:

```java
for (Vacancy vacancy : expired) {
    vacancy.setStatus(VacancyStatus.FINALIZADO);
    vacancyFinalizationNotifier.notifyApplicants(vacancy);
}
```

`notifyApplicants(Vacancy vacancy)`:
1. `vacancyApplicationRepository.findByVacancyId(vacancy.getId())`
2. Por cada `VacancyApplication`:
   - Buscar `StudentProfile` (nombre) y `User` (email) del alumno.
   - Si `accepted == true` → `mailService.sendVacancySelectedEmail(email, studentName, vacancy.getName(), companyName)` (nombre de empresa vía `CompanyService`)
   - Si no → `mailService.sendVacancyClosedEmail(email, studentName, vacancy.getName())`

No se dispara desde `updateVacancyStatus` (cierre manual de empresa) ni desde `updateVacancyStatusAdmin` — fuera de alcance, ver sección anterior.

## Testing manual

Sin testing automatizado (fuera de alcance del proyecto). Verificación manual con la skill `api-e2e` y/o Swagger UI antes de dar por cerrada la implementación: crear postulación → pasar a VISTO → verificar log de mail; forzar fecha de cierre de una vacante y correr el cron manualmente (o exponerlo transitoriamente) para verificar los mails de cierre/selección.
