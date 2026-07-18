# Endpoints — Talent API

> Referencia viva de todos los endpoints.

Índice de recursos:
1. Usuarios (`User`)
2. Alumnos (`StudentProfile`)
3. Empresas (`Company`)
4. Educacion (`Education`)
5. Experiencia laboral (`WorkExperience`)
6. Autenticacion (`Auth`)
7. Carreras (`Degree`)
8. Areas (`Area`)
9. Puestos (`Vacancy`)
10. Postulaciones (`Vacancy_Application`)
11. Registro universitario (`UniversityRegistry`)

---

## 1. Usuarios — `/user`

Controller: `user/UserController` · Tag: **Usuarios**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/user` | Crear un usuario | `CreateUserRequest` | `UserResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/user` | Listar todos los usuarios | — | `List<UserResponse>` | `200` | — |
| 3 | GET | `/user/{id}` | Obtener un usuario por id | — (path `id`) | `UserResponse` | `200` | `404` |
| 4 | GET | `/user?email={email}` | Buscar un usuario por email | — (query `email`: `@NotBlank`, `@Email`) | `UserResponse` | `200` | `400` email inválido · `404` no existe |
| 5 | PUT | `/user/{id}` | Actualizar datos editables | `UpdateUserRequest` | `UserResponse` | `200` | `400` datos inválidos · `404` no existe |
| 6 | DELETE | `/user/{id}` | Eliminar un usuario por id | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateUserRequest`** (entrada)
- `name` string · `@NotBlank`
- `surname` string · `@NotBlank`
- `email` string · `@NotBlank` `@Email`
- `password` string · `@NotBlank` `@Size(min=8)`
- `role` enum `Role` · `@NotNull` `@PublicSignupRole` (solo `ALUMNO` | `EMPRESA`)
- `phoneNumber` string? (opcional)
- `documentType` enum `DocumentType` · `@NotNull`
- `documentNumber` string · `@NotBlank`
- `linkedinUrl` string? (opcional)

**`UpdateUserRequest`** (entrada — no incluye email/password/role)
- `name` string · `@NotBlank`
- `surname` string · `@NotBlank`
- `phoneNumber` string? · `documentType` `DocumentType`? · `documentNumber` string? · `linkedinUrl` string?

**`UserResponse`** (salida — nunca expone `passwordHash`)
- `userId` · `name` · `surname` · `email` · `role` (`Role`) · `phoneNumber` · `documentType` (`DocumentType`) · `documentNumber` · `linkedinUrl` · `registeredAt` (date)

---

## 2. Alumnos — `/student-profile`

Controller: `studentprofile/StudentProfileController` · Tag: **Alumnos**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/student-profile` | Crear perfil de alumno | `CreateStudentProfileRequest` | `StudentProfileResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/student-profile` | Listar todos los perfiles | — | `List<StudentProfileResponse>` | `200` | — |
| 3 | GET | `/student-profile/{id}` | Obtener perfil por id | — (path `id`) | `StudentProfileResponse` | `200` | `404` |
| 4 | GET | `/student-profile?userId={userId}` | Perfil de un usuario | — (query `userId`: `@NotBlank`) | `StudentProfileResponse` | `200` | `400` userId inválido · `404` no existe |
| 5 | DELETE | `/student-profile/{id}` | Eliminar perfil por id | — (path `id`) | — (vacío) | `204` | `404` |

> No hay PUT de update para StudentProfile.

### Schemas

**`CreateStudentProfileRequest`** (entrada)
- `userId` string · `@NotBlank`
- `skills` `string[]?` (opcional, cargadas desde el frontend)

**`StudentProfileResponse`** (salida)
- `studentProfileId` · `userId` · `skills` (`string[]`)

---

## 3. Empresas — `/company`

Controller: `company/CompanyController` · Tag: **Empresas**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/company` | Crear empresa | `CreateCompanyRequest` | `CompanyResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/company` | Listar todas las empresas | — | `List<CompanyResponse>` | `200` | — |
| 3 | GET | `/company/{id}` | Obtener empresa por id | — (path `id`) | `CompanyResponse` | `200` | `404` |
| 4 | GET | `/company?userId={userId}` | Empresa de un usuario | — (query `userId`: `@NotBlank`) | `CompanyResponse` | `200` | `400` userId inválido · `404` no existe |
| 5 | PUT | `/company/{id}` | Actualizar empresa por id | `UpdateCompanyRequest` | `CompanyResponse` | `200` | `400` datos inválidos · `404` no existe |
| 6 | DELETE | `/company/{id}` | Eliminar empresa por id | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateCompanyRequest`** (entrada — todo obligatorio)
- `userId` `@NotBlank` · `industry` `@NotBlank` · `description` `@NotBlank` · `webUrl` `@NotBlank` · `linkedinUrl` `@NotBlank`
- `location` enum `Department` · `@NotNull`

**`UpdateCompanyRequest`** (entrada — no incluye userId)
- `industry` `@NotBlank` · `description` `@NotBlank` · `webUrl` `@NotBlank` · `linkedinUrl` `@NotBlank` · `location` `Department` `@NotNull`

**`CompanyResponse`** (salida)
- `companyId` · `userId` · `industry` · `description` · `webUrl` · `linkedinUrl` · `location` (`Department`)

---

## 4. Educacion — `/education`

Controller: `education/EducationController` · Tag: **Educacion**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/education` | Crear registro de educación | `CreateEducationRequest` | `EducationResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/education/{id}` | Obtener registro por id | — (path `id`) | `EducationResponse` | `200` | `404` |
| 3 | GET | `/education/by-id?educationId={id}` | Obtener registro por id (DTO request) | `GetEducationByIdRequest` (query `@ModelAttribute`) | `EducationResponse` | `200` | `400` parámetro inválido · `404` no existe |
| 4 | GET | `/education?studentProfileId={id}` | Listar por studentProfileId | — (query `studentProfileId`: `@NotBlank`) | `List<EducationResponse>` | `200` | `400` parámetro inválido |
| 5 | PUT | `/education/{id}` | Actualizar registro por id | `UpdateEducationRequest` | `EducationResponse` | `200` | `400` datos inválidos · `404` no existe |
| 6 | DELETE | `/education/{id}` | Eliminar registro por id | — (path `id`) | — (vacío) | `204` | `404` |

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

## 5. Experiencia laboral — `/work-experience`

Controller: `workexperience/WorkExperienceController` · Tag: **Experiencia laboral**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/work-experience` | Crear experiencia laboral | `CreateWorkExperienceRequest` | `WorkExperienceResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/work-experience/{id}` | Obtener por id | — (path `id`) | `WorkExperienceResponse` | `200` | `404` |
| 3 | GET | `/work-experience?studentProfileId={id}` | Listar por studentProfileId | — (query `studentProfileId`: `@NotBlank`) | `List<WorkExperienceResponse>` | `200` | `400` parámetro inválido |
| 4 | PUT | `/work-experience/{id}` | Actualizar por id | `UpdateWorkExperienceRequest` | `WorkExperienceResponse` | `200` | `400` datos inválidos · `404` no existe |
| 5 | DELETE | `/work-experience/{id}` | Eliminar por id | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateWorkExperienceRequest`** / **`UpdateWorkExperienceRequest`** (entrada — sin `id`)
- `studentProfileId` string · `@NotBlank`
- `company` string?
- `position` string?
- `startDate` date?
- `endDate` date? (null si es el actual)
- `description` string? (TEXT)

**`WorkExperienceResponse`** (salida)
- `workExperienceId` (PK, generado por `@PrePersist`) · `studentProfileId` · `company` · `position` · `startDate` · `endDate` · `description`

---

## 6. Autenticacion — `/auth`

Controller: `auth/AuthController` · Tag: **Autenticacion**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/auth/login` | Login con email y contraseña | `LoginRequest` | `UserResponse` | `200` | `401` credenciales incorrectas · `400` campos vacíos |

### Schemas

**`LoginRequest`** (entrada)
- `email` string · `@NotBlank`
- `password` string · `@NotBlank`

**`UserResponse`** — ver sección 1 (Usuarios).

---

## 7. Carreras — `/degree`

Controller: `degree/DegreeController` · Tag: **Carreras**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/degree` | Crear una carrera | `CreateDegreeRequest` | `DegreeResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/degree/{id}` | Obtener carrera por id | — (path `id`) | `DegreeResponse` | `200` | `404` |
| 3 | GET | `/degree` | Listar todas las carreras | — | `List<DegreeResponse>` | `200` | — |
| 4 | GET | `/degree?name={name}` | Buscar por nombre exacto | — (query `name`: `@NotBlank`) | `DegreeResponse` | `200` | `400` · `404` no existe |
| 5 | GET | `/degree?areaId={areaId}` | Listar por area | — (query `areaId`: `@NotBlank`) | `List<DegreeResponse>` | `200` | `400` |
| 6 | PUT | `/degree/{id}` | Actualizar carrera por id | `UpdateDegreeRequest` | `DegreeResponse` | `200` | `400` · `404` no existe |
| 7 | DELETE | `/degree/{id}` | Eliminar carrera por id | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateDegreeRequest`** / **`UpdateDegreeRequest`** (entrada)
- `areaId` string · `@NotBlank`
- `name` string · `@NotBlank` `@Size(max=100)`
- `isUcu` boolean · `@NotNull`

**`DegreeResponse`** (salida)
- `degreeId` · `areaId` · `name` · `isUcu`

---

## 8. Areas — `/area`

Controller: `area/AreaController` · Tag: **Areas**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/area` | Crear un area | `CreateAreaRequest` | `AreaResponse` | `201` | `400` · `404` el area padre no existe |
| 2 | GET | `/area` | Listar todas las areas | — | `List<AreaResponse>` | `200` | — |
| 3 | GET | `/area/{id}` | Obtener area por id | — (path `id`) | `AreaResponse` | `200` | `404` |
| 4 | PUT | `/area/{id}` | Actualizar area por id | `UpdateAreaRequest` | `AreaResponse` | `200` | `400` · `404` no existe |
| 5 | DELETE | `/area/{id}` | Eliminar area por id | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateAreaRequest`** / **`UpdateAreaRequest`** (entrada)
- `name` string · `@NotBlank`
- `parentAreaId` string? (null si es un area raíz)

**`AreaResponse`** (salida)
- `areaId` · `name` · `parentAreaId` (null si es raíz)

---

## 9. Puestos — `/vacancy`

Controller: `vacancy/VacancyController` · Tag: **Puestos**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/vacancy` | Crear un puesto | `CreateVacancyRequest` | `VacancyResponse` | `201` | `400` · `404` company/area no existe |
| 2 | GET | `/vacancy` | Listar todos los puestos | — | `List<VacancyResponse>` | `200` | — |
| 3 | GET | `/vacancy/{id}` | Obtener puesto por id | — (path `id`) | `VacancyResponse` | `200` | `404` |
| 4 | GET | `/vacancy?status={status}` | Listar por estado | — (query `status`: `VacancyStatus`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 5 | GET | `/vacancy?companyId={id}` | Listar por empresa | — (query `companyId`: `@NotBlank`) | `List<VacancyResponse>` | `200` | `400` |
| 6 | GET | `/vacancy?areaId={id}` | Listar por area | — (query `areaId`: `@NotBlank`) | `List<VacancyResponse>` | `200` | `400` |
| 7 | GET | `/vacancy?modality={modality}` | Listar por modalidad | — (query `modality`: `Modality`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 8 | GET | `/vacancy?location={location}` | Listar por localidad | — (query `location`: `Departamento`) | `List<VacancyResponse>` | `200` | `400` enum inválido |
| 9 | PUT | `/vacancy/{id}` | Actualizar puesto por id | `CreateVacancyRequest` | `VacancyResponse` | `200` | `400` · `404` no existe / company/area no existe |
| 10 | DELETE | `/vacancy/{id}` | Eliminar puesto por id | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateVacancyRequest`** (entrada — también usado en el PUT)
- `companyId` string · `@NotBlank` · `areaId` string · `@NotBlank`
- `location` enum `Departamento` · `@NotNull` · `modality` enum `Modality` · `@NotNull`
- `status` enum `VacancyStatus`? (en el POST se fuerza a `PENDIENTE`)
- `name` · `description` · `requirements` · `contractType` · `salaryRange` — todos string `@NotBlank`
- `publicationDate` date? · `closingDate` date?

**`VacancyResponse`** (salida)
- `vacancyId` · `companyId` · `areaId` · `publicationDate` · `closingDate` · `location` (`Departamento`) · `modality` (`Modality`) · `status` (`VacancyStatus`) · `name` · `description` · `requirements` · `contractType` · `salaryRange`

---

## 10. Postulaciones — `/vacancy-application`

Controller: `vacancyapplication/VacancyApplicationController` · Tag: **Postulaciones**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/vacancy-application` | Crear una postulación | `CreateVacancyApplicationRequest` | `VacancyApplicationResponse` | `201` | `400` · `404` la vacante no existe |
| 2 | GET | `/vacancy-application/{id}` | Obtener postulación por id | — (path `id`) | `VacancyApplicationResponse` | `200` | `404` |
| 3 | GET | `/vacancy-application` | Listar todas las postulaciones | — | `List<VacancyApplicationResponse>` | `200` | — |
| 4 | GET | `/vacancy-application?vacancyId={id}` | Listar por vacante | — (query `vacancyId`: `@NotBlank`) | `List<VacancyApplicationResponse>` | `200` | `400` |
| 5 | GET | `/vacancy-application?studentProfileId={id}` | Listar por perfil de alumno | — (query `studentProfileId`: `@NotBlank`) | `List<VacancyApplicationResponse>` | `200` | `400` |
| 6 | GET | `/vacancy-application?status={status}` | Listar por estado | — (query `status`: `VacancyApplicationStatus`) | `List<VacancyApplicationResponse>` | `200` | `400` enum inválido |
| 7 | PUT | `/vacancy-application/{id}` | Actualizar el estado por id | `UpdateVacancyApplicationRequest` | `VacancyApplicationResponse` | `200` | `400` · `404` no existe |
| 8 | DELETE | `/vacancy-application/{id}` | Eliminar postulación por id | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateVacancyApplicationRequest`** (entrada)
- `vacancyId` string · `@NotBlank` · `studentProfileId` string · `@NotBlank`
- `status` enum `VacancyApplicationStatus`? · `appliedAt` date?

**`UpdateVacancyApplicationRequest`** (entrada)
- `status` enum `VacancyApplicationStatus`

**`VacancyApplicationResponse`** (salida)
- `vacancyApplicationId` · `vacancyId` · `studentProfileId` · `status` (`VacancyApplicationStatus`) · `appliedAt` (date)

---

## 11. Registro universitario — `/university-registry`

Controller: `universityregistry/UniversityRegistryController` · Tag: **University Registry**

| # | Método | Path | Descripción | Request schema | Response schema | Happy | No happy |
|---|--------|------|-------------|----------------|-----------------|-------|----------|
| 1 | POST | `/university-registry` | Crear un registro | `CreateUniversityRegistryRequest` | `UniversityRegistryResponse` | `201` | `400` datos inválidos |
| 2 | GET | `/university-registry` | Listar todos los registros | — | `List<UniversityRegistryResponse>` | `200` | — |
| 3 | GET | `/university-registry/{id}` | Obtener registro por id | — (path `id`) | `UniversityRegistryResponse` | `200` | `404` |
| 4 | PUT | `/university-registry/{id}` | Actualizar registro por id | `UpdateUniversityRegistryRequest` | `UniversityRegistryResponse` | `200` | `400` · `404` no existe |
| 5 | DELETE | `/university-registry/{id}` | Eliminar registro por id | — (path `id`) | — (vacío) | `204` | `404` |

### Schemas

**`CreateUniversityRegistryRequest`** / **`UpdateUniversityRegistryRequest`** (entrada)
- `documentType` enum `DocumentType` (`common.DocumentType`: `CEDULA_IDENTIDAD | PASAPORTE | DNI`)
- `documentNumber` · `name` · `surname` (string)

**`UniversityRegistryResponse`** (salida)
- `universityRegistryId` · `documentType` (`DocumentType`) · `documentNumber` · `name` · `surname`

---

## Enums de referencia

- **`Role`**: `ALUMNO`, `EMPRESA`, `ADMIN` (registro público solo `ALUMNO` | `EMPRESA`)
- **`DocumentType`**: `CEDULA_IDENTIDAD`, `PASAPORTE`, `DNI` — enum **único compartido** en
  `common.DocumentType`, usado por User y UniversityRegistry
- **`Education.DegreeLevel`**: `TECNICATURA`, `LICENCIATURA`, `GRADO`, `POSGRADO`, `DOCTORADO`
- **`VacancyStatus`**: `PENDIENTE`, `FINALIZADO`
- **`VacancyApplicationStatus`**: `PENDIENTE`, `VISTO`, `FINALIZADO`
- **`Modality`**: `PRESENCIAL`, `HIBRIDO`, `REMOTO`
- **`Departamento`** (localidad de Vacancy, 19): mismos valores que `Department`
- **`Department`** (19): `ARTIGAS`, `CANELONES`, `CERRO_LARGO`, `COLONIA`, `DURAZNO`, `FLORES`, `FLORIDA`, `LAVALLEJA`, `MALDONADO`, `MONTEVIDEO`, `PAYSANDU`, `RIO_NEGRO`, `RIVERA`, `ROCHA`, `SALTO`, `SAN_JOSE`, `SORIANO`, `TACUAREMBO`, `TREINTA_Y_TRES`

---

