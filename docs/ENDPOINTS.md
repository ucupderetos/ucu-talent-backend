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
13. **Dev (TEMPORAL)**

---

## 1. Usuarios — `/user`

Controller: `user/UserController` · Tag: **Usuarios**

`User` es solo identidad + autenticación tras el refactor: `email`, `passwordHash`, `role`,
`status`, `registeredAt`. Los datos personales viven en `StudentProfile`/`Company`/`Admin`
(perfiles con PK compartida).

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/user` | Crear una cuenta (paso 1 del registro) | 🌐 Público | `CreateUserRequest` | `UserResponse` | `201` | `400` datos inválidos · `409` email duplicado |
| 2 | GET | `/user` | Listar todas las cuentas | 🔒 Autenticado | — | `List<UserResponse>` | `200` | — |
| 3 | GET | `/user/{id}` | Obtener una cuenta por id | 🔒 Autenticado | — (path `id`) | `UserResponse` | `200` | `404` |
| 4 | GET | `/user?email={email}` | Buscar por email | 🔒 Autenticado | — (query `email`: `@NotBlank`, `@Email`) | `UserResponse` | `200` | `400` email inválido · `404` no existe |
| 5 | DELETE | `/user/{id}` | Eliminar una cuenta | 🔒 + dueño | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` no existe |

### Schemas

**`CreateUserRequest`** (entrada — solo credenciales, el perfil se crea en un paso 2 aparte)
- `email` string · `@NotBlank` `@Email`
- `password` string · `@NotBlank` `@Size(min=8)`
- `role` enum `Role` · `@NotNull` `@PublicSignupRole` (solo `ALUMNO` | `EMPRESA`; `ADMIN` no se crea por acá)

**`UserResponse`** (salida — nunca expone `passwordHash`)
- `userId` · `email` · `role` (`Role`) · `status` (`AccountStatus`) · `registeredAt` (date)

---

## 2. Alumnos — `/student-profile`

Controller: `studentprofile/StudentProfileController` · Tag: **Alumnos**

Paso 2 del registro de un `ALUMNO`. `userId` en el body se ignora siempre — se deriva del
token.

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/student-profile` | Crear perfil de alumno | 🔒 rol `ALUMNO` | `CreateStudentProfileRequest` | `StudentProfileResponse` | `201` | `400` · `409` ya tiene perfil |
| 2 | GET | `/student-profile` | Listar todos los perfiles | 🔒 Autenticado | — | `List<StudentProfileResponse>` | `200` | — |
| 3 | GET | `/student-profile/{id}` | Obtener perfil por id | 🔒 Autenticado | — (path `id`) | `StudentProfileResponse` | `200` | `404` |
| 4 | GET | `/student-profile?userId={userId}` | Perfil de un usuario (PK compartida: equivale a `getById`) | 🔒 Autenticado | — (query `userId`: `@NotBlank`) | `StudentProfileResponse` | `200` | `400` · `404` no existe |
| 5 | DELETE | `/student-profile/{id}` | Eliminar perfil por id | 🔒 + dueño | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` no existe |

> No hay `PUT` de update para `StudentProfile` todavía.

### Schemas

**`CreateStudentProfileRequest`** (entrada — `userId` se ignora, sale del token)
- `userId` string · `@NotBlank` (decorativo, ver arriba)
- `name` string · `@NotBlank` · `surname` string · `@NotBlank`
- `documentType` enum `DocumentType` · `@NotNull`
- `documentNumber` string · `@NotBlank`
- `phoneNumber` string? (opcional) · `linkedinUrl` string? (opcional)
- `skills` `string[]?` (opcional)

**`StudentProfileResponse`** (salida — no expone `userId`, la PK ya lo es)
- `studentProfileId` (= `userId`) · `name` · `surname` · `documentType` (`DocumentType`) · `documentNumber` · `phoneNumber` · `linkedinUrl` · `skills` (`string[]`)

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

### Schemas

**`CreateCompanyRequest`** (entrada — `userId` se ignora, sale del token)
- `userId` `@NotBlank` (decorativo) · `name` `@NotBlank` (razón social)
- `industry` `@NotBlank` · `description` `@NotBlank` · `webUrl` `@NotBlank` · `linkedinUrl` `@NotBlank`
- `location` enum `Department` · `@NotNull`

**`UpdateCompanyRequest`** (entrada — no incluye `userId`)
- `name` `@NotBlank` · `industry` `@NotBlank` · `description` `@NotBlank` · `webUrl` `@NotBlank` · `linkedinUrl` `@NotBlank` · `location` `Department` `@NotNull`

**`CompanyResponse`** (salida — no expone `userId` ni `approved`)
- `companyId` (= `userId`) · `name` · `industry` · `description` · `webUrl` · `linkedinUrl` · `location` (`Department`)

---

## 4. Admins — `/admin`

Controller: `admin/AdminController` · Tag: **Admins**

Perfil del `ADMIN` (PK compartida con `User`, mismo patrón que `StudentProfile`/`Company`).


| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/admin` | Crear el admin del usuario logueado | 🔒 rol `ADMIN` | `CreateAdminRequest` | `AdminResponse` | `201` | `400` · `403` rol incorrecto · `409` ya tiene admin |
| 2 | GET | `/admin` | Listar todos los admins | 🔒 Autenticado | — | `List<AdminResponse>` | `200` | — |
| 3 | GET | `/admin/{id}` | Obtener admin por id | 🔒 Autenticado | — (path `id`) | `AdminResponse` | `200` | `404` |
| 4 | GET | `/admin?userId={userId}` | Admin de un usuario (PK compartida: equivale a `getById`) | 🔒 Autenticado | — (query `userId`: `@NotBlank`) | `AdminResponse` | `200` | `400` · `404` no existe |
| 5 | PUT | `/admin/{id}` | Actualizar admin por id | 🔒 + dueño | `UpdateAdminRequest` | `AdminResponse` | `200` | `400` · `403` no es el dueño · `404` no existe |
| 6 | DELETE | `/admin/{id}` | Eliminar admin por id | 🔒 + dueño | — (path `id`) | — (vacío) | `204` | `403` no es el dueño · `404` no existe |

### Schemas

**`CreateAdminRequest`** (entrada — `userId` se ignora, sale del token)
- `userId` `@NotBlank` (decorativo) · `name` `@NotBlank` · `surname` `@NotBlank`

**`UpdateAdminRequest`** (entrada — no incluye `userId`)
- `name` `@NotBlank` · `surname` `@NotBlank`

**`AdminResponse`** (salida — no expone `userId`, la PK ya lo es)
- `adminId` (= `userId`) · `name` · `surname`

---

## 5. Educacion — `/education`

Controller: `education/EducationController` · Tag: **Educacion**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/education` | Crear registro de educación | ⚠️ Sin restricción | `CreateEducationRequest` | `EducationResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/education/{id}` | Obtener registro por id | 🔒 Autenticado | — (path `id`) | `EducationResponse` | `200` | `404` |
| 3 | GET | `/education/by-id?educationId={id}` | Obtener registro por id (DTO request) | 🔒 Autenticado | `GetEducationByIdRequest` (query `@ModelAttribute`) | `EducationResponse` | `200` | `400` parámetro inválido · `404` no existe |
| 4 | GET | `/education?studentProfileId={id}` | Listar por studentProfileId | 🔒 Autenticado | — (query `studentProfileId`: `@NotBlank`) | `List<EducationResponse>` | `200` | `400` parámetro inválido |
| 5 | PUT | `/education/{id}` | Actualizar registro por id | ⚠️ Sin restricción | `UpdateEducationRequest` | `EducationResponse` | `200` | `400` · `404` no existe |
| 6 | DELETE | `/education/{id}` | Eliminar registro por id | ⚠️ Sin restricción | — (path `id`) | — (vacío) | `204` | `404` |

> **Regresión temporal e intencional** (ver `.claude/TODO.md`): el ownership de `create`/`update`/`delete`
> se sacó a propósito para coordinar con el compañero dueño de este paquete. Hoy **cualquier
> usuario logueado puede editar/borrar la educación de cualquier alumno**. No tocar sin avisar.

### Schemas

**`CreateEducationRequest`** / **`UpdateEducationRequest`** (entrada — sin `id`)
- `studentProfileId` string · `@NotBlank`
- `degreeLevel` enum `DegreeLevel` · `@NotNull` — `TECNICATURA | LICENCIATURA | GRADO | POSGRADO | DOCTORADO`
- `degreeId` string · `@NotBlank`
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

`update`/`delete` derivan el dueño de la fila **ya persistida** (buscan por el `id` de la
ruta y comparan `studentProfileId` real contra el JWT) — no de un campo del body. Antes de
hoy comparaban contra un campo que mandaba el cliente, lo que permitía que cualquier alumno
editara/borrara la experiencia de otro (IDOR, arreglado esta sesión).

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/work-experience` | Crear experiencia laboral | 🔒 + dueño (`studentProfileId` del body debe ser el propio) | `CreateWorkExperienceRequest` | `WorkExperienceResponse` | `201` | `400` · `403` no es el dueño |
| 2 | GET | `/work-experience/{id}` | Obtener por id | 🔒 Autenticado | — (path `id`) | `WorkExperienceResponse` | `200` | `404` |
| 3 | GET | `/work-experience?studentProfileId={id}` | Listar por studentProfileId | 🔒 Autenticado | — (query `studentProfileId`: `@NotBlank`) | `List<WorkExperienceResponse>` | `200` | `400` parámetro inválido |
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
| 1 | POST | `/auth/login` | Login con email y contraseña | 🌐 Público | `LoginRequest` | `UserResponse` | `200` (+ `Set-Cookie` httpOnly) | `401` credenciales incorrectas · `400` campos vacíos |
| 2 | POST | `/auth/logout` | Vencer la cookie de sesión | 🌐 Público | — | — (vacío) | `200` (+ cookie vencida) | — |
| 3 | GET | `/me` | Datos de la cuenta logueada (hidrata el front) | 🔒 Autenticado | — | `MeResponse` | `200` | `401` sin cookie o token inválido/vencido |

### Schemas

**`LoginRequest`** (entrada)
- `email` string · `@NotBlank` · `password` string · `@NotBlank`

**`UserResponse`** — ver sección 1 (Usuarios).

**`MeResponse`** (salida — se lee `status` fresco de la BD, nunca del JWT)
- `userId` · `email` · `role` (`Role`) · `status` (`AccountStatus`) · `registeredAt` (date)

---

## 8. Carreras — `/degree`

Controller: `degree/DegreeController` · Tag: **Carreras**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/degree` | Crear una carrera | ⚠️ Sin restricción | `CreateDegreeRequest` | `DegreeResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/degree/{id}` | Obtener carrera por id | 🔒 Autenticado | — (path `id`) | `DegreeResponse` | `200` | `404` |
| 3 | GET | `/degree` | Listar todas las carreras | 🔒 Autenticado | — | `List<DegreeResponse>` | `200` | — |
| 4 | GET | `/degree?name={name}` | Buscar por nombre exacto | 🔒 Autenticado | — (query `name`: `@NotBlank`) | `DegreeResponse` | `200` | `400` · `404` no existe |
| 5 | GET | `/degree?areaId={areaId}` | Listar por area | 🔒 Autenticado | — (query `areaId`: `@NotBlank`) | `List<DegreeResponse>` | `200` | `400` |
| 6 | PUT | `/degree/{id}` | Actualizar carrera por id | ⚠️ Sin restricción | `UpdateDegreeRequest` | `DegreeResponse` | `200` | `400` · `404` no existe |
| 7 | DELETE | `/degree/{id}` | Eliminar carrera por id | ⚠️ Sin restricción | — (path `id`) | — (vacío) | `204` | `404` |

> Debería ser `hasRole("ADMIN")` en `POST`/`PUT`/`DELETE` (roadmap #1, dificultad 3/10,
> pendiente). Hoy cualquier rol logueado puede crear/editar/borrar carreras.

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

> Mismo gap que `Degree` — debería ser `hasRole("ADMIN")` (roadmap #1).

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
| 1 | POST | `/vacancy` | Crear un puesto | 🔒 rol `EMPRESA` + empresa `APROBADO` | `CreateVacancyRequest` | `VacancyResponse` | `201` | `400` · `403` empresa no aprobada · `404` company/area no existe |
| 2 | GET | `/vacancy` | Listar todos los puestos | 🔒 Autenticado | — | `List<VacancyResponse>` | `200` | — |
| 3 | GET | `/vacancy/{id}` | Obtener puesto por id | 🔒 Autenticado | — (path `id`) | `VacancyResponse` | `200` | `404` |
| 4 | GET | `/vacancy?status={status}` | Listar por estado | 🔒 Autenticado | — (query `status`: `VacancyStatus`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 5 | GET | `/vacancy?companyId={id}` | Listar por empresa | 🔒 Autenticado | — (query `companyId`: `@NotBlank`) | `List<VacancyResponse>` | `200` | `400` |
| 6 | GET | `/vacancy?areaId={id}` | Listar por area | 🔒 Autenticado | — (query `areaId`: `@NotBlank`) | `List<VacancyResponse>` | `200` | `400` |
| 7 | GET | `/vacancy?modality={modality}` | Listar por modalidad | 🔒 Autenticado | — (query `modality`: `Modality`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 8 | GET | `/vacancy?location={location}` | Listar por localidad | 🔒 Autenticado | — (query `location`: `Departamento`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 9 | PUT | `/vacancy/{id}` | Actualizar puesto por id | 🔒 rol `EMPRESA` (⚠️ sin ownership, ver aviso arriba) | `CreateVacancyRequest` | `VacancyResponse` | `200` | `400` · `403` empresa no aprobada · `404` no existe / company/area no existe |
| 10 | DELETE | `/vacancy/{id}` | Eliminar puesto por id | 🔒 rol `EMPRESA` (⚠️ sin ownership, ver aviso arriba) | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateVacancyRequest`** (entrada — también usado en el `PUT`)
- `companyId` string · `@NotBlank` · `areaId` string · `@NotBlank`
- `location` enum `Departamento` · `@NotNull` · `modality` enum `Modality` · `@NotNull`
- `status` enum `VacancyStatus`? (en el `POST` se fuerza a `PENDIENTE`)
- `name` · `description` · `requirements` · `contractType` · `salaryRange` — todos string `@NotBlank`
- `publicationDate` date? · `closingDate` date?

**`VacancyResponse`** (salida)
- `vacancyId` · `companyId` · `areaId` · `publicationDate` · `closingDate` · `location` (`Departamento`) · `modality` (`Modality`) · `status` (`VacancyStatus`) · `name` · `description` · `requirements` · `contractType` · `salaryRange`

---

## 11. Postulaciones — `/vacancy-application`

Controller: `vacancyapplication/VacancyApplicationController` · Tag: **Postulaciones**


| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/vacancy-application` | Crear una postulación | 🔒 rol `ALUMNO` + alumno `APROBADO` | `CreateVacancyApplicationRequest` | `VacancyApplicationResponse` | `201` | `400` · `403` alumno no aprobado · `404` vacante/perfil no existe · `409` ya postulado |
| 2 | GET | `/vacancy-application/{id}` | Obtener postulación por id | 🔒 Autenticado | — (path `id`) | `VacancyApplicationResponse` | `200` | `404` |
| 3 | GET | `/vacancy-application` | Listar todas las postulaciones | 🔒 Autenticado | — | `List<VacancyApplicationResponse>` | `200` | — |
| 4 | GET | `/vacancy-application?vacancyId={id}` | Listar por vacante | 🔒 Autenticado | — (query `vacancyId`: `@NotBlank`) | `List<VacancyApplicationResponse>` | `200` | `400` |
| 5 | GET | `/vacancy-application?studentProfileId={id}` | Listar por perfil de alumno | 🔒 Autenticado | — (query `studentProfileId`: `@NotBlank`) | `List<VacancyApplicationResponse>` | `200` | `400` |
| 6 | GET | `/vacancy-application?status={status}` | Listar por estado | 🔒 Autenticado | — (query `status`: `VacancyApplicationStatus`) | `List<VacancyApplicationResponse>` | `200` | `400` enum inválido |
| 7 | PUT | `/vacancy-application/{id}` | Actualizar el estado por id | 🔒 + dueño (empresa dueña de la vacante) | `UpdateVacancyApplicationRequest` | `VacancyApplicationResponse` | `200` | `400` · `403` no es la empresa dueña · `404` no existe · `409` transición inválida (retrocede) |
| 8 | DELETE | `/vacancy-application/{id}` | Eliminar postulación por id | 🔒 + dueño (alumno postulante) | — (path `id`) | — (vacío) | `204` | `403` no es el postulante · `404` no existe |

### Schemas

**`CreateVacancyApplicationRequest`** (entrada — `studentProfileId` se ignora, sale del token)
- `vacancyId` string · `@NotBlank` · `studentProfileId` string · `@NotBlank` (decorativo)
- `status` enum `VacancyApplicationStatus`? · `appliedAt` date?

**`UpdateVacancyApplicationRequest`** (entrada)
- `status` enum `VacancyApplicationStatus`

**`VacancyApplicationResponse`** (salida)
- `vacancyApplicationId` · `vacancyId` · `studentProfileId` · `status` (`VacancyApplicationStatus`) · `appliedAt` (date)

---

## 12. Registro universitario — `/university-registry`

Controller: `universityregistry/UniversityRegistryController` · Tag: **University Registry**

| # | Método | Path | Descripción | Permisos | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------|----------------|-----------------|-------|----------|
| 1 | POST | `/university-registry` | Crear un registro | ⚠️ Sin restricción | `CreateUniversityRegistryRequest` | `UniversityRegistryResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/university-registry` | Listar todos los registros | 🔒 Autenticado | — | `List<UniversityRegistryResponse>` | `200` | — |
| 3 | GET | `/university-registry/{id}` | Obtener registro por id | 🔒 Autenticado | — (path `id`) | `UniversityRegistryResponse` | `200` | `404` |
| 4 | PUT | `/university-registry/{id}` | Actualizar registro por id | ⚠️ Sin restricción | `UpdateUniversityRegistryRequest` | `UniversityRegistryResponse` | `200` | `400` · `404` no existe |
| 5 | DELETE | `/university-registry/{id}` | Eliminar registro por id | ⚠️ Sin restricción | — (path `id`) | — (vacío) | `204` | `404` |


### Schemas

**`CreateUniversityRegistryRequest`** / **`UpdateUniversityRegistryRequest`** (entrada)
- `documentType` enum `DocumentType` (`common.DocumentType`: `CEDULA_IDENTIDAD | PASAPORTE | DNI`)
- `documentNumber` · `name` · `surname` (string)

**`UniversityRegistryResponse`** (salida)
- `universityRegistryId` · `documentType` (`DocumentType`) · `documentNumber` · `name` · `surname`

---

## 13. Dev — **TEMPORAL, borrar antes de cualquier entorno compartido/prod**

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

## Enums de referencia

- **`Role`**: `ALUMNO`, `EMPRESA`, `ADMIN` (registro público solo `ALUMNO` | `EMPRESA`; `ADMIN` vía sección 13, temporal)
- **`AccountStatus`**: `PENDIENTE`, `APROBADO`, `RECHAZADO` — reemplaza a `Company.approved`, aplica a los tres roles. Toda cuenta nace `PENDIENTE` (el `ADMIN` de `/dev/admin` nace `APROBADO` directo).
- **`DocumentType`**: `CEDULA_IDENTIDAD`, `PASAPORTE`, `DNI` — enum **único compartido** en
  `common.DocumentType`, usado por `StudentProfile` y `UniversityRegistry`
- **`Education.DegreeLevel`**: `TECNICATURA`, `LICENCIATURA`, `GRADO`, `POSGRADO`, `DOCTORADO`
- **`VacancyStatus`**: `PENDIENTE`, `FINALIZADO` (falta `RECHAZADO`, roadmap #3)
- **`VacancyApplicationStatus`**: `PENDIENTE`, `VISTO`, `FINALIZADO` (falta `ACEPTADO`/`RECHAZADO`, roadmap #3) — la transición no retrocede (ver sección 11)
- **`Modality`**: `PRESENCIAL`, `HIBRIDO`, `REMOTO`
- **`Departamento`** (localidad de Vacancy, 19): mismos valores que `Department`
- **`Department`** (19): `ARTIGAS`, `CANELONES`, `CERRO_LARGO`, `COLONIA`, `DURAZNO`, `FLORES`, `FLORIDA`, `LAVALLEJA`, `MALDONADO`, `MONTEVIDEO`, `PAYSANDU`, `RIO_NEGRO`, `RIVERA`, `ROCHA`, `SALTO`, `SAN_JOSE`, `SORIANO`, `TACUAREMBO`, `TREINTA_Y_TRES`

---
