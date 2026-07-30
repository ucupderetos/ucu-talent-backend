# Endpoints — Talent API



## Permisos

| Símbolo | Significado |
|---------|-------------|
| 🌐 Público | No requiere sesión. |
| 🔒 Autenticado | Requiere cookie de sesión válida, cualquier rol. |
| 🔒 rol `X` | Requiere sesión + `hasRole("X")`. |
| 🔒 + dueño | Requiere sesión y ser el dueño del recurso o equivalente.|
| ⚠️ Sin restricción | Autenticado alcanza — **gap de seguridad conocido** |

Índice de recursos:
1. Usuarios (`User`)
2. Alumnos (`StudentProfile`)
3. Empresas (`Company`)
4. Admins (`Admin`)
5. Educacion (`Education`)
6. Experiencia laboral (`WorkExperience`)
7. Autenticacion (`Auth`)
8. Carreras (`Degree`)
9. Areas (`Area`)
10. Puestos (`Vacancy`)
11. Postulaciones (`Vacancy_Application`)
12. Registro universitario (`UniversityRegistry`)
13. Auditoria (`AuditLog`)
14. **Dev (TEMPORAL)**
15. Templates de mail (`MailTemplate`)
16. **Storage (ADMIN)**

---

## 1. Usuarios — `/user`

Controller: `user/UserController` · Tag: **Usuarios**

`User` es solo identidad + autenticación tras el refactor: `email`, `passwordHash`, `role`,
`status`, `registeredAt`. Los datos personales viven en `StudentProfile`/`Company`/`Admin`
(perfiles con PK compartida).

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/user` | Crear una cuenta (paso 1 del registro) | 🌐 Público | `CreateUserRequest` | `UserResponse` | `201` | `400` datos inválidos · `409` email duplicado · `429` demasiadas altas (rate limit) |
| 2 | GET | `/user` | Listar cuentas, filtro opcional por `status`/`role` | 🔒 rol `ADMIN` | — (query `status`? `AccountStatus`, `role`? `Role`) | `List<UserResponse>` | `200` | — |
| 3 | GET | `/user/{id}` | Obtener una cuenta por id | 🔒 Autenticado | — (path `id`) | `UserResponse` | `200` | `404` |
| 4 | GET | `/user/mail?email={email}` | Buscar por email | 🔒 rol `ADMIN` | — (query `email`: `@NotBlank`, `@Email`) | `UserResponse` | `200` | `400` email inválido · `403` no es ADMIN · `404` no existe |
| 5 | GET | `/user/profile-image?profileObject={objectName}` | Obtener URL firmada de la foto de perfil | 🔒 Autenticado | — (query `profileObject`) | `String` (URL firmada) | `200` | `400` formato inválido · `403` no autenticado · `404` no existe |
| 6 | PATCH | `/user/{id}` | Aprobar o rechazar un usuario | 🔒 rol `ADMIN` | `UpdateUserStatusRequest` | `UserResponse` | `200` | `400` · `403` no es ADMIN · `404` no existe · `409` transición inválida |
| 7 | PATCH | `/user/profile/image` | Subir o reemplazar la foto de perfil (siempre la propia, sale del JWT) | 🔒 Autenticado | `multipart/form-data` — `file` | `UserResponse` | `200` | `400` · `401` no autenticado · `404` no existe |
| 8 | DELETE | `/user/{id}` | Eliminar una cuenta | 🔒 + dueño | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` no existe |
| 9 | DELETE | `/user/profile/image` | Eliminar la foto de perfil (siempre la propia, sale del JWT) | 🔒 Autenticado | — | — (vacío) | `204` | `401` no autenticado · `404` no existe |


### Schemas

**`CreateUserRequest`** (entrada — solo credenciales, el perfil se crea en un paso 2 aparte)
- `email` string · `@NotBlank` `@Email`
- `password` string · `@NotBlank` `@Size(min=8)`
- `role` enum `Role` · `@NotNull` `@PublicSignupRole` (solo `ALUMNO` | `EMPRESA`; `ADMIN` no se crea por acá) — `role: ALUMNO` nace con `status: APROBADO`, `role: EMPRESA` nace con `status: PENDIENTE`

**`UpdateUserStatusRequest`** (entrada)
- `status` enum `AccountStatus` · `@NotNull` (solo `APROBADO` | `RECHAZADO`)
- `adminComment` string? (opcional) — se guarda en `StudentProfile.adminComment`/`Company.adminComment`
  según el rol del usuario (no aplica a `ADMIN`, no tiene perfil asociado)

**`UserResponse`** (salida — nunca expone `passwordHash`)
- `userId` · `email` · `role` (`Role`) · `status` (`AccountStatus`) · `registeredAt` (date) · `profileImage` (null hasta que suba una imagen)

---

## 2. Alumnos — `/student-profile`

Controller: `studentprofile/StudentProfileController` · Tag: **Alumnos**

Paso 2 del registro de un `ALUMNO`. El id del perfil sale siempre del token (no hay campo
`userId` en el body de creación).

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/student-profile` | Crear perfil de alumno | 🔒 rol `ALUMNO` | `CreateStudentProfileRequest` | `StudentProfileResponse` | `201` | `400` datos inválidos o documento con formato inválido · `409` ya tiene perfil o documento duplicado |
| 2 | GET | `/student-profile?status={status}` | Listar perfiles, opcionalmente filtrados por estado | 🔒 rol `ADMIN` | — (query `status`: `AccountStatus`, opcional) | `List<StudentProfileResponse>` | `200` | `400` enum inválido · `403` no es ADMIN |
| 3 | GET | `/student-profile/{id}` | Obtener perfil por id | 🔒 Autenticado | — (path `id`) | `StudentProfileResponse` | `200` | `404` |
| 4 | GET | `/student-profile?userId={userId}` | Perfil de un usuario (PK compartida: equivale a `getById`) | 🔒 rol `ADMIN` | — (query `userId`: `@NotBlank`) | `StudentProfileResponse` | `200` | `400` · `403` no es ADMIN · `404` no existe |
| 5 | GET | `/student-profile/status-summary` | Totales de alumnos por estado | 🔒 rol `ADMIN` | — | `StudentProfileStatusSummaryResponse` | `200` | `403` no es ADMIN |
| 6 | GET | `/student-profile/cv?cvFile={objectName}` | Obtener una URL firmada del CV | 🔒 rol `EMPRESA` | — (query `cvFile`) | `String` (URL firmada) | `200` | `401` no autenticado · `403` no es EMPRESA · `404` no existe CV |
| 7 | PUT | `/student-profile/{id}` | Actualizar telefono, LinkedIn, skills y descripción por id | 🔒 + dueño | `UpdateStudentProfileRequest` | `StudentProfileResponse` | `200` | `400` · `403` no es el dueño · `404` no existe |
| 8 | PATCH | `/student-profile/cv` | Subir o reemplazar el CV (siempre el propio, sale del JWT) | 🔒 + dueño (implícito, sin `id` en el path) | `multipart/form-data` — `file` (solo PDF) | `StudentProfileResponse` | `200` | `400` solo se permite PDF · `401` no autenticado · `404` no existe |
| 9 | DELETE | `/student-profile/{id}` | Eliminar perfil por id | 🔒 + dueño | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` no existe |
| 10 | DELETE | `/student-profile/cv` | Eliminar el CV (siempre el propio, sale del JWT) | 🔒 + dueño (implícito) | — | — (vacío) | `204` | `401` no autenticado · `404` no existe |

### Schemas

**`CreateStudentProfileRequest`** (entrada — sin `userId`, el id sale del token; `@ValidDocumentNumber`)
- `name` string · `@NotBlank` · `surname` string · `@NotBlank`
- `documentType` enum `DocumentType` · `@NotNull`
- `documentNumber` string · `@NotBlank` · `@ValidDocumentNumber` valida el formato según `documentType`
  (`CEDULA_IDENTIDAD`/`DNI`: solo dígitos · `PASAPORTE`: alfanumérico · `400` si no matchea) — acepta
  `.`, `-` y espacios (ej. `"1.234.567-8"`), se normalizan y se guardan sin ellos (ej. `"12345678"`).
  Duplicado de `documentType` + `documentNumber` (ya normalizado) → `409`; el mismo número con
  `documentType` distinto es válido.
- `phoneNumber` string? (opcional) · `linkedinUrl` string? (opcional)
- `skills` `string[]?` (opcional) · `description` string? (opcional)

**`UpdateStudentProfileRequest`** (entrada — 4 campos, todos obligatorios)
- `phoneNumber` string · `@NotBlank` · `linkedinUrl` string · `@NotBlank` · `skills` `string[]` · `@NotEmpty` · `description` string · `@NotBlank`

**`StudentProfileResponse`** (salida — no expone `userId`, la PK ya lo es)
- `studentProfileId` (= `userId`) · `email` (del `User` dueño) · `registeredAt` (date, del `User` dueño) ·
  `name` · `surname` · `documentType` (`DocumentType`) · `documentNumber`
  (normalizado, sin `.`/`-`/espacios aunque se haya mandado con ellos) · `phoneNumber` · `linkedinUrl` ·
  `skills` (`string[]`) · `status` (`AccountStatus`, del `User` dueño) · `description` · `reviewedAt`
  (date, null hasta que el Admin revise) · `adminComment` (null hasta que el Admin revise) · `cvFile` (null hasta que suba un CV)

**`StudentProfileStatusSummaryResponse`** (salida)
- `total` · `pendiente` · `aprobado` · `rechazado` (todos `long`, cuentas de `User.status` filtradas por rol `ALUMNO`)

---

## 3. Empresas — `/company`

Controller: `company/CompanyController` · Tag: **Empresas**


| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/company` | Crear empresa | 🔒 rol `EMPRESA` | `CreateCompanyRequest` | `CompanyResponse` | `201` | `400` |
| 2 | GET | `/company` | Listar todas las empresas | 🔒 Autenticado | — | `List<CompanyResponse>` | `200` | — |
| 3 | GET | `/company/{id}` | Obtener empresa por id | 🔒 Autenticado | — (path `id`) | `CompanyResponse` | `200` | `404` |
| 4 | GET | `/company?userId={userId}` | Empresa de un usuario (PK compartida: equivale a `getById`) | 🔒 Autenticado | — (query `userId`: `@NotBlank`) | `CompanyResponse` | `200` | `400` · `404` no existe |
| 5 | PUT | `/company/{id}` | Actualizar empresa por id | 🔒 + dueño | `UpdateCompanyRequest` | `CompanyResponse` | `200` | `400` · `403` no es el dueño · `404` no existe |
| 6 | DELETE | `/company/{id}` | Eliminar empresa por id | 🔒 + dueño | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` no existe |
| 7 | GET | `/company/status-summary` | Totales de empresas por estado | 🔒 rol `ADMIN` | — | `CompanyStatusSummaryResponse` | `200` | `403` no es ADMIN |

### Schemas

**`CreateCompanyRequest`** (entrada — sin `userId`, el id sale del token)
- `name` `@NotBlank` (razón social)
- `industry` `@NotBlank` · `description` `@NotBlank` · `webUrl` `@NotBlank` · `linkedinUrl` `@NotBlank`
- `location` enum `Department` · `@NotNull`

**`UpdateCompanyRequest`** (entrada — no incluye `userId`)
- `name` `@NotBlank` · `industry` `@NotBlank` · `description` `@NotBlank` · `webUrl` `@NotBlank` · `linkedinUrl` `@NotBlank` · `location` `Department` `@NotNull`

**`CompanyResponse`** (salida — no expone `userId`)
- `companyId` (= `userId`) · `name` · `industry` · `description` · `webUrl` · `linkedinUrl` · `location` (`Department`) · `status` (`AccountStatus`, del `User` dueño) · `reviewedAt` (date, null hasta que el Admin revise) · `adminComment` (null hasta que el Admin revise)

**`CompanyStatusSummaryResponse`** (salida)
- `total` · `pendiente` · `aprobado` · `rechazado` (todos `long`, cuentas de `User.status` filtradas por rol `EMPRESA`)

---

## 4. Admins — `/admin`

Controller: `admin/AdminController` · Tag: **Admins**

Perfil del `ADMIN` (PK compartida con `User`, mismo patrón que `StudentProfile`/`Company`).


| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/admin` | Crear el admin del usuario logueado | 🔒 rol `ADMIN` | `CreateAdminRequest` | `AdminResponse` | `201` | `400` · `403` rol incorrecto · `409` ya tiene admin |
| 2 | GET | `/admin/dashboard` | Totales y listados de la pantalla inicial del admin | 🔒 rol `ADMIN` | — | `AdminDashboardResponse` | `200` | `403` no es ADMIN |
| 3 | GET | `/admin` | Listar todos los admins | 🔒 rol `ADMIN` | — | `List<AdminResponse>` | `200` | `403` no es ADMIN |
| 4 | GET | `/admin/{id}` | Obtener admin por id | 🔒 Autenticado | — (path `id`) | `AdminResponse` | `200` | `404` |
| 5 | GET | `/admin?userId={userId}` | Admin de un usuario (PK compartida: equivale a `getById`) | 🔒 Autenticado | — (query `userId`: `@NotBlank`) | `AdminResponse` | `200` | `400` · `404` no existe |
| 6 | PUT | `/admin/{id}` | Actualizar admin por id | 🔒 + dueño | `UpdateAdminRequest` | `AdminResponse` | `200` | `400` · `403` no es el dueño · `404` no existe |
| 7 | DELETE | `/admin/{id}` | Eliminar admin por id | 🔒 + dueño | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` no existe |

### Schemas

**`CreateAdminRequest`** (entrada — sin `userId`, el id sale del token)
- `name` `@NotBlank` · `surname` `@NotBlank`

**`UpdateAdminRequest`** (entrada — no incluye `userId`)
- `name` `@NotBlank` · `surname` `@NotBlank`

**`AdminDashboardResponse`** (salida)
- `counts.companies` `{ total, pendientes }` — `pendientes` = empresas cuya cuenta esta en `PENDIENTE`
- `counts.vacancies` `{ total, publicadas }` — `total` excluye las borradas (`deleted = true`)
- `counts.applications` `{ total, pendientes }` · `counts.users` `{ total, alumnos, empresas, admins }`
- `applicationStatusSummary` `[{ status: VacancyApplicationStatus, count: long }]` — siempre los 3 estados, aunque den 0
- `recentVacancies` `[{ vacancyId, name, companyName, publicationDate, status, applicationCount }]` — tope 5, `publicationDate` DESC, sin borradas
- `pendingCompanies` `[{ companyId, name, industry, registeredAt }]` — tope 10, `registeredAt` DESC

**`AdminResponse`** (salida — no expone `userId`, la PK ya lo es)
- `adminId` (= `userId`) · `name` · `surname`

---

## 5. Educacion — `/education`

Controller: `education/EducationController` · Tag: **Educacion**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/education` | Crear registro de educación | ⚠️ Sin restricción | `CreateEducationRequest` | `EducationResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/education` | Listar todos los registros de educación | 🔒 rol `ADMIN` | — | `List<EducationResponse>` | `200` | `403` no es ADMIN |
| 3 | GET | `/education/{id}` | Obtener registro por id | 🔒 Autenticado | — (path `id`) | `EducationResponse` | `200` | `404` |
| 4 | GET | `/education/by-id?educationId={id}` | Obtener registro por id (DTO request) | 🔒 Autenticado | `GetEducationByIdRequest` (query `@ModelAttribute`) | `EducationResponse` | `200` | `400` parámetro inválido · `404` no existe |
| 5 | GET | `/education?studentProfileId={id}` | Listar por studentProfileId | 🔒 Autenticado | — (query `studentProfileId`: `@NotBlank`) | `List<EducationResponse>` | `200` | `400` parámetro inválido |
| 6 | PUT | `/education/{id}` | Actualizar registro por id | ⚠️ Sin restricción | `UpdateEducationRequest` | `EducationResponse` | `200` | `400` · `404` no existe |
| 7 | DELETE | `/education/{id}` | Eliminar registro por id | ⚠️ Sin restricción | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateEducationRequest`** / **`UpdateEducationRequest`** (entrada — sin `id`)
- `studentProfileId` string · `@NotBlank`
- `degreeLevel` enum `DegreeLevel` · `@NotNull` — `TECNICATURA | LICENCIATURA | GRADO | POSGRADO | DOCTORADO`
- `degreeId` string · `@NotBlank`
- `institution` string? (obligatoria si la carrera no es UCU)
- `description` string? (TEXT)
- `startDate` date · `@NotNull`
- `endDate` date? (null si está en curso)

**`EducationResponse`** (salida)
- `educationId` (PK, generado por `@PrePersist`) · `studentProfileId` · `degreeLevel` (`DegreeLevel`) · `degreeId` · `description` · `startDate` · `endDate`

**`GetEducationByIdRequest`** (query)
- `educationId` string · `@NotBlank`

---

## 6. Experiencia laboral — `/work-experience`

Controller: `workexperience/WorkExperienceController` · Tag: **Experiencia laboral**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/work-experience` | Crear experiencia laboral | 🔒 + dueño (`studentProfileId` del body debe ser el propio) | `CreateWorkExperienceRequest` | `WorkExperienceResponse` | `201` | `400` · `403` no es el dueño |
| 2 | GET | `/work-experience/me/{id}` | Obtener por id | 🔒 + dueño | — (path `id`) | `WorkExperienceResponse` | `200` | `403` no es el dueño · `404` |
| 3 | GET | `/work-experience?studentProfileId={id}` | Listar por studentProfileId | 🌐 Público | — (query `studentProfileId`: `@NotBlank`) | `List<WorkExperienceResponse>` | `200` | `400` parámetro inválido |
| 4 | PUT | `/work-experience/{id}` | Actualizar por id | 🔒 + dueño (contra la fila real) | `UpdateWorkExperienceRequest` | `WorkExperienceResponse` | `200` | `400` · `403` no es el dueño · `404` no existe |
| 5 | DELETE | `/work-experience/{id}` | Eliminar por id | 🔒 + dueño (contra la fila real) | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` no existe |

### Schemas

**`CreateWorkExperienceRequest`** (entrada — sin `id`)
- `studentProfileId` string · `@NotBlank`
- `company` string? · `position` string? · `startDate` date? · `endDate` date? (null si es el actual) · `description` string? (TEXT)

**`UpdateWorkExperienceRequest`** (entrada — **sin `studentProfileId`**: el dueño no se
reasigna en un update, se toma de la fila existente)
- `company` string? · `position` string? · `startDate` date? · `endDate` date? · `description` string? (TEXT)

**`WorkExperienceResponse`** (salida)
- `workExperienceId` (PK, generado por `@PrePersist`) · `studentProfileId` · `company` · `position` · `startDate` · `endDate` · `description`

---

## 7. Autenticacion — `/auth`, `/me`

Controller: `auth/AuthController` (`/auth`) + `auth/MeController` (`/me`) · Tag: **Autenticacion**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/auth/login` | Login con email y contraseña | 🌐 Público | `LoginRequest` | `UserResponse` | `200` (+ `Set-Cookie` httpOnly) | `401` credenciales incorrectas · `400` campos vacíos · `429` demasiados intentos (rate limit) |
| 2 | POST | `/auth/logout` | Vencer la cookie de sesión | 🌐 Público | — | — (vacío) | `200` (+ cookie vencida) | — |
| 3 | GET | `/me` | Datos de la cuenta logueada (hidrata el front) | 🔒 Autenticado | — | `MeResponse` | `200` | `401` sin cookie o token inválido/vencido |

### Schemas

**`LoginRequest`** (entrada)
- `email` string · `@NotBlank` · `password` string · `@NotBlank`

**`UserResponse`** — ver sección 1 (Usuarios).

**`MeResponse`** (salida — se lee `status` fresco de la BD, nunca del JWT)
- `userId` · `email` · `role` (`Role`) · `status` (`AccountStatus`) · `registeredAt` (date)
- `hasProfile` boolean — si ya existe el perfil del paso 2 del registro (`StudentProfile`/`Company`/`Admin` según `role`).

### 7.4 Rate limiting de login

- El endpoint `POST /auth/login` tiene límite doble en memoria (Bucket4j + Caffeine):
  - Por email: 5 intentos por 60 segundos.
  - Por IP: 20 intentos por 60 segundos.
- Si se excede cualquiera de los dos límites, responde:
  - `429 Too Many Requests`
  - Body `application/problem+json` (`ProblemDetail`)
  - Header `Retry-After` (segundos de bloqueo)
- Bloqueo progresivo (estilo lockout de iOS): cada vez que la misma key (email o IP) vuelve a exceder su límite, el castigo escala un escalón en vez de repetirse: 30s → 3min → 15min (configurable, se mantiene en el último escalón si sigue reincidiendo). El contador de reincidencia de una key se resetea solo si pasa 24h sin nuevas infracciones.
- Cloud Run: al usar contadores en memoria, con N instancias el límite efectivo agregado puede acercarse a `N x límite`.
  - Para demo estricta de rate limit: configurar `min-instances=1` y `max-instances=1`.
  - Si se escala a múltiples instancias, este rate limit debe considerarse aproximado.

---

## 8. Carreras — `/degree`

Controller: `degree/DegreeController` · Tag: **Carreras**

| # | Método | Path | Descripción | Permisos | Request schema                   | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------------------------|-----------------|-------|----------|
| 1 | POST | `/degree` | Crear una carrera | ⚠️ Sin restricción | `CreateDegreeRequest`            | `DegreeResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/degree/{id}` | Obtener carrera por id | 🔒 Autenticado | — (path `id`)                    | `DegreeResponse` | `200` | `404` |
| 3 | GET | `/degree` | Listar todas las carreras | 🔒 Autenticado | —                                | `List<DegreeResponse>` | `200` | — |
| 4 | GET | `/degree?areaId={areaId}` | Listar por area | 🔒 Autenticado | — (query La`areaId`: `@NotBlank`) | `List<DegreeResponse>` | `200` | `400` |
| 5 | PUT | `/degree/{id}` | Actualizar carrera por id | ⚠️ Sin restricción | `UpdateDegreeRequest`            | `DegreeResponse` | `200` | `400` · `404` no existe |
| 6 | DELETE | `/degree/{id}` | Eliminar carrera por id | ⚠️ Sin restricción | — (path `id`)                    | — (vacío) | `204` | `404` |

> `name` se normaliza en `create`/`update` (trim + primera letra en mayúscula). No es único.

### Schemas

**`CreateDegreeRequest`** / **`UpdateDegreeRequest`** (entrada)
- `areaId` string · `@NotBlank` · `name` string · `@NotBlank` `@Size(max=100)` · `isUcu` boolean · `@NotNull`

**`DegreeResponse`** (salida)
- `degreeId` · `areaId` · `name` · `isUcu`

---

## 9. Areas — `/area`

Controller: `area/AreaController` · Tag: **Areas**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/area` | Crear un area | ⚠️ Sin restricción | `CreateAreaRequest` | `AreaResponse` | `201` | `400` · `404` el area padre no existe |
| 2 | GET | `/area` | Listar todas las areas | 🔒 Autenticado | — | `List<AreaResponse>` | `200` | — |
| 3 | GET | `/area/{id}` | Obtener area por id | 🔒 Autenticado | — (path `id`) | `AreaResponse` | `200` | `404` |
| 4 | PUT | `/area/{id}` | Actualizar area por id | ⚠️ Sin restricción | `UpdateAreaRequest` | `AreaResponse` | `200` | `400` · `404` no existe |
| 5 | DELETE | `/area/{id}` | Eliminar area por id | ⚠️ Sin restricción | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateAreaRequest`** / **`UpdateAreaRequest`** (entrada)
- `name` string · `@NotBlank` · `parentAreaId` string? (null si es un area raíz)

**`AreaResponse`** (salida)
- `areaId` · `name` · `parentAreaId` (null si es raíz)

---

## 10. Puestos — `/vacancy`

Controller: `vacancy/VacancyController` · Tag: **Puestos**


| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/vacancy` | Crear un puesto | 🔒 rol `EMPRESA` + dueño de `companyId` + empresa `APROBADO` | `CreateVacancyRequest` | `VacancyResponse` | `201` | `400` · `403` no es el dueño / empresa no aprobada · `404` company/area no existe |
| 2 | GET | `/vacancy` | Listar todos los puestos | 🔒 Autenticado | — | `List<VacancyResponse>` | `200` | — |
| 3 | GET | `/vacancy/{id}` | Obtener puesto por id | 🔒 Autenticado | — (path `id`) | `VacancyResponse` | `200` | `404` |
| 4 | GET | `/vacancy/{id}/resolved` | Obtener puesto por id con la empresa y el area resueltos | 🔒 Autenticado | — (path `id`) | `ResolvedVacancyResponse` | `200` | `404` |
| 5 | GET | `/vacancy/status/{status}` | Listar por estado | 🔒 Autenticado | — (path `status`: `VacancyStatus`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 6 | GET | `/vacancy/company/{companyId}` | Listar por empresa | 🔒 Autenticado | — (path `companyId`: `@NotBlank`) | `List<VacancyResponse>` | `200` | `400` |
| 7 | GET | `/vacancy/company/{companyId}/management` | Listar puestos de una empresa para gestión | 🔒 dueño de `companyId` o rol `ADMIN` | — (path `companyId`: `@NotBlank`) | `List<VacancyManagementResponse>` | `200` | `400` · `403` no es el dueño ni ADMIN · `404` company no existe |
| 8 | GET | `/vacancy/area/{areaId}` | Listar por area | 🔒 Autenticado | — (path `areaId`: `@NotBlank`) | `List<VacancyResponse>` | `200` | `400` |
| 9 | GET | `/vacancy/modality/{modality}` | Listar por modalidad | 🔒 Autenticado | — (path `modality`: `Modality`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 10 | GET | `/vacancy/location/{location}` | Listar por localidad | 🔒 Autenticado | — (path `location`: `Departamento`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 11 | PUT | `/vacancy/{id}` | Actualizar puesto por id | 🔒 rol `EMPRESA` + dueño | `UpdateVacancyRequest` | `VacancyResponse` | `200` | `400` · `403` no es el dueño · `404` no existe |
| 12 | PATCH | `/vacancy/status/{id}` | Cambiar estado del puesto (empresa) | 🔒 rol `EMPRESA` + dueño | `UpdateVacancyStatusRequest` | `VacancyResponse` | `200` | `400` · `403` no es el dueño · `404` no existe |
| 13 | PUT | `/vacancy/status/{id}` | Cambiar estado del puesto (admin) | 🔒 rol `ADMIN` | `UpdateVacancyStatusAdminRequest` | `VacancyResponse` | `200` | `400` · `403` no es ADMIN · `404` no existe |
| 14 | DELETE | `/vacancy/{id}` | Eliminar puesto por id | 🔒 rol `EMPRESA` + dueño | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` |
| 15 | GET | `/vacancy/status-summary` | Totales de puestos por estado | 🔒 rol `ADMIN` | — | `VacancyStatusSummaryResponse` | `200` | `403` no es ADMIN |

### Schemas

**`CreateVacancyRequest`** (entrada)
- `companyId` string · `@NotBlank` · `areaId` string · `@NotBlank`
- `location` enum `Departamento` · `@NotNull` · `modality` enum `Modality` · `@NotNull`
- `status` enum `VacancyStatus`? (en el `POST` se fuerza a `PUBLICADO`)
- `name` · `description` · `requirements` · `contractType` · `salary` — todos string `@NotBlank`
- `publicationDate` date? · `closingDate` date?

**`UpdateVacancyRequest`** (entrada — sin `companyId`/`areaId`, no se reasignan)
- Update parcial: **todos los campos son opcionales**, sin validaciones. Un campo en `null` no se modifica.
- `publicationDate` date? · `closingDate` date?
- `location` enum `Departamento`? · `modality` enum `Modality`?
- `name`? · `description`? · `requirements`? · `salary`? — string · `contractType` enum `ContractType`?

**`UpdateVacancyStatusRequest`** (entrada)
- `status` enum `VacancyStatus` · `@NotNull`

**`UpdateVacancyStatusAdminRequest`** (entrada)
- `adminComment` string? · `status` enum `VacancyStatus` · `@NotNull`

**`VacancyResponse`** (salida)
- `vacancyId` · `companyId` · `areaId` · `publicationDate` · `closingDate` · `location` (`Departamento`) · `modality` (`Modality`) · `status` (`VacancyStatus`) · `name` · `description` · `requirements` · `contractType` · `salary`

**`ResolvedVacancyResponse`** (salida)
- `vacancy` objeto `VacancyResponse` · `company` objeto `CompanyPublicResponse`
- `areaName` string · `parentAreaName` string? (`null` si el area es de primer nivel)

**`CompanyPublicResponse`** (salida — `CompanyResponse` sin los campos de moderación)
- `companyId` · `name` · `industry` · `description` · `webUrl` · `linkedinUrl` — string
- `location` enum `Departamento` · `status` enum `AccountStatus`
- No incluye `reviewedAt` ni `adminComment`.

**`VacancyManagementResponse`** (salida)
- `vacancy` objeto `VacancyResponse` · `companyName` string · `areaName` string
- `applicationCount` `long` · `newApplicationsCount` `long` (postulaciones en `PENDIENTE`)

**`VacancyStatusSummaryResponse`** (salida)
- `total` · `pendiente` · `publicado` · `finalizado` (todos `long`)

---

## 11. Postulaciones — `/vacancy-application`

Controller: `vacancyapplication/VacancyApplicationController` · Tag: **Postulaciones**


| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/vacancy-application` | Crear una postulación | 🔒 rol `ALUMNO` + alumno `APROBADO` | `CreateVacancyApplicationRequest` | `VacancyApplicationResponse` | `201` | `400` · `403` alumno no aprobado · `404` vacante/perfil no existe · `409` ya postulado |
| 2 | GET | `/vacancy-application/me` | Listar mis postulaciones (alumno autenticado) | 🔒 rol `ALUMNO` | — | `List<VacancyApplicationStudentResponse>` | `200` | `403` no es ALUMNO |
| 3 | GET | `/vacancy-application/me/detailed` | Listar mis postulaciones con el puesto, la empresa y el area resueltos | 🔒 rol `ALUMNO` | — | `List<MyApplicationRowResponse>` | `200` | `403` no es ALUMNO |
| 4 | GET | `/vacancy-application/{id}` | Obtener postulación por id | 🔒 + dueño (empresa dueña de la vacante) | — (path `id`) | `VacancyApplicationResponse` | `200` | `403` no es la empresa dueña · `404` no existe |
| 5 | GET | `/vacancy-application` | Listar todas las postulaciones | 🔒 rol `ADMIN` | — | `List<VacancyApplicationResponse>` | `200` | `403` no es ADMIN |
| 6 | GET | `/vacancy-application?vacancyId={id}` | Listar por vacante | 🔒 + dueño (empresa dueña de la vacante) | — (query `vacancyId`: `@NotBlank`) | `List<VacancyApplicationResponse>` | `200` | `400` · `403` no es la empresa dueña · `404` no existe la vacante |
| 7 | GET | `/vacancy-application/detailed?vacancyId={id}` | Listar postulaciones de una vacante con datos del alumno, la vacante y la empresa | 🔒 dueño (empresa dueña de la vacante) o rol `ADMIN` | — (query `vacancyId`: `@NotBlank`) | `List<ApplicationListItemResponse>` | `200` | `400` · `403` no es la empresa dueña ni ADMIN · `404` no existe la vacante |
| 8 | GET | `/vacancy-application/detailed` | Listar todas las postulaciones con datos del alumno, la vacante y la empresa | 🔒 rol `ADMIN` | — | `List<ApplicationListItemResponse>` | `200` | `403` no es ADMIN |
| 9 | GET | `/vacancy-application?studentProfileId={id}` | Listar por perfil de alumno | 🔒 rol `ADMIN` | — (query `studentProfileId`: `@NotBlank`) | `List<VacancyApplicationResponse>` | `200` | `400` · `403` no es ADMIN |
| 10 | GET | `/vacancy-application?status={status}` | Listar por estado | 🔒 rol `ADMIN` | — (query `status`: `VacancyApplicationStatus`) | `List<VacancyApplicationResponse>` | `200` | `400` enum inválido · `403` no es ADMIN |
| 11 | PUT | `/vacancy-application/{id}` | Actualizar el estado por id | 🔒 + dueño (empresa dueña de la vacante) | `UpdateVacancyApplicationRequest` | `VacancyApplicationResponse` | `200` | `400` · `403` no es la empresa dueña · `404` no existe · `409` transición inválida (retrocede) |
| 12 | PATCH | `/vacancy-application/{id}/accept` | Marcar la postulación como aceptada (`accepted: true`) | 🔒 + dueño (empresa dueña de la vacante) | — (path `id`) | `VacancyApplicationResponse` | `200` | `403` no es la empresa dueña · `404` no existe |
| 13 | DELETE | `/vacancy-application/{id}` | Eliminar postulación por id | 🔒 + dueño (alumno postulante) | — (path `id`) | — (vacío) | `204` | `403` no es el postulante · `404` no existe |
| 14 | GET | `/vacancy-application/status-summary` | Totales de postulaciones por estado | 🔒 rol `ADMIN` | — | `VacancyApplicationStatusSummaryResponse` | `200` | `403` no es ADMIN |

### Schemas

**`CreateVacancyApplicationRequest`** (entrada — `studentProfileId` se ignora, sale del token)
- `vacancyId` string · `@NotBlank` · `studentProfileId` string · `@NotBlank` (decorativo)
- `status` enum `VacancyApplicationStatus`? · `appliedAt` date?

**`UpdateVacancyApplicationRequest`** (entrada)
- `status` enum `VacancyApplicationStatus`

**`VacancyApplicationResponse`** (salida — visible para empresa dueña y ADMIN)
- `vacancyApplicationId` · `vacancyId` · `studentProfileId` · `status` (`VacancyApplicationStatus`) · `appliedAt` (date) · `accepted` boolean (default `false`; solo se pasa a `true` vía `PATCH /vacancy-application/{id}/accept`)

**`VacancyApplicationStudentResponse`** (salida — usado solo en `GET /vacancy-application/me`, nunca expone `accepted`)
- `vacancyApplicationId` · `vacancyId` · `vacancyName` · `companyId` · `companyName` · `appliedAt` (date) · `status` (`VacancyApplicationStatus`) · `vacancyStatus` (`VacancyStatus`)

**`ApplicationListItemResponse`** (salida)
- `application` objeto `VacancyApplicationResponse`
- `studentName` string · `studentSurname` string · `studentEmail` string
- `vacancyId` string · `vacancyName` string
- `companyId` string · `companyName` string

**`MyApplicationRowResponse`** (salida)
- `application` objeto `VacancyApplicationStudentResponse`
- `vacancy` objeto `VacancyResponse` (ver sección 10)
- `companyName` string · `areaName` string
- No incluye `accepted` en ningún nivel.

**`VacancyApplicationStatusSummaryResponse`** (salida)
- `total` · `pendiente` · `visto` · `finalizado` (todos `long`)

---

## 12. Registro universitario — `/university-registry`

Controller: `universityregistry/UniversityRegistryController` · Tag: **University Registry**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/university-registry` | Crear un registro | 🔒 rol `ADMIN` | `CreateUniversityRegistryRequest` | `UniversityRegistryResponse` | `201` | `400` datos inválidos o documento con formato inválido · `403` no es ADMIN |
| 2 | GET | `/university-registry` | Listar todos los registros | 🔒 rol `ADMIN` | — | `List<UniversityRegistryResponse>` | `200` | `403` no es ADMIN |
| 3 | GET | `/university-registry/{id}` | Obtener registro por id | 🔒 rol `ADMIN` | — (path `id`) | `UniversityRegistryResponse` | `200` | `403` no es ADMIN · `404` |
| 4 | PUT | `/university-registry/{id}` | Actualizar registro por id | 🔒 rol `ADMIN` | `UpdateUniversityRegistryRequest` | `UniversityRegistryResponse` | `200` | `400` datos inválidos o documento con formato inválido · `403` no es ADMIN · `404` no existe |
| 5 | DELETE | `/university-registry/{id}` | Eliminar registro por id | 🔒 rol `ADMIN` | — (path `id`) | — (vacío) | `204` | `403` no es ADMIN · `404` |


### Schemas

**`CreateUniversityRegistryRequest`** / **`UpdateUniversityRegistryRequest`** (entrada — `@ValidDocumentNumber`)
- `documentType` enum `DocumentType` (`common.DocumentType`: `CEDULA_IDENTIDAD | PASAPORTE | DNI`)
- `documentNumber` · `@ValidDocumentNumber` valida el formato según `documentType`
  (`CEDULA_IDENTIDAD`/`DNI`: solo dígitos · `PASAPORTE`: alfanumérico · `400` si no matchea) — acepta
  `.`, `-` y espacios (ej. `"1.234.567-8"`), se normalizan y se guardan sin ellos (ej. `"12345678"`)
- `name` · `surname` (string)

**`UniversityRegistryResponse`** (salida)
- `universityRegistryId` · `documentType` (`DocumentType`) · `documentNumber` (normalizado, sin
  `.`/`-`/espacios aunque se haya mandado con ellos) · `name` · `surname`

---

## 13. Auditoria — `/audit`

Controller: `audit/AuditController` · Tag: **Auditoria**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | GET | `/audit` | Ver los ultimos 50 registros de auditoria | ⚠️ Sin restricción | — | `List<AuditLog>` | `200` | — |

### Schemas

**`AuditLog`** (salida — entidad expuesta directamente, no tiene DTO propio)
- campos según `audit/AuditLog.java`

---

## 14. Dev — **TEMPORAL, borrar antes de cualquier entorno compartido/prod**

Controller: `dev/DevAdminController` · Tag: **Dev (temporal)**

⚠️ **Este endpoint es de desarrollo.**

Para borrarlo: el paquete `dev/` completo, `"/dev/**"` de
`SecurityConfig.PUBLIC_MATCHER`, y `UserService.createAdmin(...)` + su impl.

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/dev/admin` | Crear un ADMIN (cuenta + perfil, en una transacción) | 🌐 **Público, sin candado** | `CreateDevAdminRequest` | `UserResponse` | `201` | `400` · `409` email duplicado |

### Schemas

**`CreateDevAdminRequest`** (entrada)
- `email` string · `@NotBlank` `@Email` · `password` string · `@NotBlank` `@Size(min=8)`
- `name` string · `@NotBlank` · `surname` string · `@NotBlank`

**`UserResponse`** — ver sección 1 (Usuarios). El admin creado nace `status: APROBADO`.

---

## 15. Templates de mail — `/mail-template`

Controller: `mail/MailTemplateController` · Tag: **Templates de mail**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | GET | `/mail-template` | Listar todos los templates de mail | 🔒 rol `ADMIN` | — | `List<MailTemplateResponse>` | `200` | `403` no es ADMIN |
| 2 | GET | `/mail-template/{code}` | Obtener un template por código | 🔒 rol `ADMIN` | — (path `code`: `MailTemplateCode`) | `MailTemplateResponse` | `200` | `400` código inválido · `403` no es ADMIN · `404` no existe |
| 3 | PUT | `/mail-template/{code}` | Actualizar subject/body de un template | 🔒 rol `ADMIN` | `UpdateMailTemplateRequest` | `MailTemplateResponse` | `200` | `400` datos inválidos o código inválido · `403` no es ADMIN · `404` no existe |

### Schemas

**`UpdateMailTemplateRequest`** (entrada)
- `subject` string · `@NotBlank` `@Size(max=200)`
- `body` string · `@NotBlank` (admite placeholders `{{variable}}`)

**`MailTemplateResponse`** (salida)
- `mailTemplateId` · `code` (`MailTemplateCode`) · `subject` · `body` · `placeholders` `string[]`
  (de solo lectura, fijo por `code`)

---

## Enums de referencia

- **`Role`**: `ALUMNO`, `EMPRESA`, `ADMIN` (registro público solo `ALUMNO` | `EMPRESA`; `ADMIN` vía sección 13, temporal)
- **`AccountStatus`**: `PENDIENTE`, `APROBADO`, `RECHAZADO` — reemplaza a `Company.approved`, aplica a los tres roles. Al registrarse (`POST /user`): `ALUMNO` nace `APROBADO`, `EMPRESA` nace `PENDIENTE` (el `ADMIN` de `/dev/admin` nace `APROBADO` directo). Transición vía `PATCH /user/{id}`: desde `APROBADO`/`RECHAZADO` el Admin puede alternar libremente entre ambos (reversible); nunca se puede volver a `PENDIENTE` (`409`).
- **`DocumentType`**: `CEDULA_IDENTIDAD`, `PASAPORTE`, `DNI` — enum **único compartido** en
  `common.DocumentType`, usado por `StudentProfile` y `UniversityRegistry`
- **`Education.DegreeLevel`**: `TECNICATURA`, `LICENCIATURA`, `GRADO`, `POSGRADO`, `DOCTORADO`
- **`VacancyStatus`**: `PENDIENTE`, `FINALIZADO` (falta `RECHAZADO`)
- **`VacancyApplicationStatus`**: `PENDIENTE`, `VISTO`, `FINALIZADO` (falta `ACEPTADO`/`RECHAZADO`) — la transición no retrocede (ver sección 11)
- **`Modality`**: `PRESENCIAL`, `HIBRIDO`, `REMOTO`
- **`Departamento`** (localidad de Vacancy, 19): mismos valores que `Department`
- **`Department`** (19): `ARTIGAS`, `CANELONES`, `CERRO_LARGO`, `COLONIA`, `DURAZNO`, `FLORES`, `FLORIDA`, `LAVALLEJA`, `MALDONADO`, `MONTEVIDEO`, `PAYSANDU`, `RIO_NEGRO`, `RIVERA`, `ROCHA`, `SALTO`, `SAN_JOSE`, `SORIANO`, `TACUAREMBO`, `TREINTA_Y_TRES`
- **`MailTemplateCode`**: `NEW_APPLICATION`, `APPLICATION_VISTO`, `VACANCY_CLOSED`, `VACANCY_SELECTED` — códigos fijos, sin create/delete por API

---

## 16. Storage — `/storage`

Controller: `storage/StorageController` · Tag: **Storage**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/storage/files` | Subir un archivo a Google Cloud Storage | 🔒 rol `ADMIN` | `multipart/form-data` — `file`, query `field` (`@NotBlank`) | `StorageUploadResponse` | `201` | `400` archivo/field inválido |
| 2 | DELETE | `/storage/images?objectName={objectName}` | Eliminar un archivo/imagen por nombre de objeto | 🔒 rol `ADMIN` | — (query `objectName`: `@NotBlank`) | — (vacío) | `204` | `400` · `404` no existe |

### Schemas

**`StorageUploadResponse`** (salida)
- `objectName` · `originalFilename` · `contentType` · `size` (long) · `bucket` · `gcsUri` · `publicUrl`