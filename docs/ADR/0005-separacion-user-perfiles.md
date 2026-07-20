# 0005 — Separación de `User` y sus perfiles (`StudentProfile` / `Company`)

- **Estado:** propuesto
- **Fecha:** 2026-07-19
- **Contexto previo:** ADR 0001 (arquitectura en 3 capas), ADR 0004 (migraciones Flyway)

## Contexto

Hoy la tabla `"user"` concentra atributos que solo aplican a algunos roles:

- `surname` es `NOT NULL` (V6) pero no significa nada para una empresa: toda cuenta
  EMPRESA guarda un apellido inventado.
- `document_type` / `document_number` son nullable **solo** porque las empresas no
  tienen cédula. La columna es nullable por el rol, no porque el dato sea opcional.
- La razón social de la empresa no tiene columna propia: vive en `"user".name`.
- `linkedin_url` está **duplicado** en `"user"` (V6) y en `company` (V5).

El síntoma común: la BD acepta estados imposibles y las validaciones de qué campo es
obligatorio se van al service en forma de `if (role == ALUMNO)`. Es lógica que el
esquema podría garantizar y no garantiza.

Se suma un requerimiento nuevo: **el alumno también requiere aprobación** antes de
poder postularse (a futuro, verificable automáticamente contra `UniversityRegistry`
cruzando la cédula). Hoy `approved` existe solo en `company` (V9).

## Decisión

### 1. `"user"` queda como identidad y autenticación

```
user_id, email, password_hash, role, status, registered_at
```

Ninguna columna nullable. `"user"` responde una sola pregunta: quién sos, cómo
probás que sos vos, y si tu cuenta está habilitada.

### 2. Los atributos de negocio bajan al perfil que los necesita

```
student_profile: name, surname, document_type, document_number,
                 phone_number, linkedin_url, skills
company:         legal_name, industry, description, web_url,
                 linkedin_url, location
```

`legal_name` es columna nueva. `company.user_id` se elimina (redundante con la PK
compartida, ver punto 4).

### 3. `status` sube a `"user"` y reemplaza a `company.approved`

```sql
status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
  CHECK (status IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
```

El criterio para subir un campo al padre no es *"¿todos los roles lo tienen?"* sino
**"¿significa algo para todos los roles?"**. `surname` en una empresa no significa
nada; `status` en un admin sí (se crea por seed, nace `APROBADO`).

Tres estados y no un booleano porque `false` mezclaría "todavía no lo revisamos" con
"lo revisamos y no pasa" — el front necesita mensajes distintos y moderación necesita
saber qué cuentas están en cola.

`INACTIVO` se evaluó y **se descarta**: pertenece a otro eje (ciclo de vida de una
cuenta ya admitida, no proceso de admisión). Si aparece la necesidad, la salida limpia
es un campo `deactivated_at`, que preserva el estado de admisión.

### 4. La PK compartida pasa a ser real (PK + FK)

Hoy `student_profile.student_profile_id == user_id` es convención sostenida por el
mapper. Pasa a estar garantizada por el esquema:

```sql
ALTER TABLE company
  ADD CONSTRAINT fk_company_user
  FOREIGN KEY (company_id) REFERENCES "user"(user_id) ON DELETE CASCADE;
```

El 1-a-1 y el borrado en cascada quedan a cargo de Postgres.

### 5. El perfil se crea con datos reales, no como placeholder

**Esta es la decisión que hace viable el resto.** `UserRegistrationServiceImpl:35-37`
hoy crea un perfil con todos los campos en `null`; por eso V18 tuvo que bajar los
`NOT NULL` de `company`. Mantener ese patrón obligaría a dejar todo
`student_profile` nullable, reproduciendo exactamente el problema que este ADR
resuelve.

En su lugar: **el signup crea solo la fila de `"user"` (en `PENDIENTE`). La fila del
perfil se inserta en el paso 2, ya con los datos completos.** La ausencia de perfil es
un estado legítimo y legible: una cuenta `PENDIENTE` sin perfil es una cuenta que no
terminó de registrarse.

Esto permite `NOT NULL` real en los campos obligatorios de cada perfil, y es agnóstico
de cómo el front implemente el registro: si manda todo junto, `UserRegistrationService`
hace los dos inserts en la misma `@Transactional`; si lo parte en dos pantallas, el
paso 2 es un `POST /student-profile` autenticado.

### 6. `status` NO va como claim del JWT

Se evaluó y se descarta. El token dura 4 h (`jwt.expiration-minutes`, default 240) y
**no hay mecanismo de revocación**. Un `status` en el token quedaría desactualizado
hasta 4 h: una cuenta recién aprobada seguiría bloqueada, y — peor — una cuenta
rechazada conservaría el acceso.

Regla: **en el token va solo lo que no cambia dentro de la vida del token.** `role`
califica; `status` no.

`status` se chequea **fresco contra la BD en el service**, siguiendo el patrón que ya
existe en `VacancyServiceImpl.requireApprovedCompany(...)`.

### 7. Un alumno no `APROBADO` no puede postularse

`VacancyApplicationServiceImpl.create()` gana un `requireApprovedStudent(...)`,
espejo del `requireApprovedCompany(...)` de `VacancyServiceImpl`.

## Alternativas consideradas

**Dejar la tabla compartida, aflojando `surname` a nullable y agregando
`company.legal_name`.** Migración chica y sin refactor. Se descarta porque no resuelve
el fondo: quedan columnas "nullable según el rol" y la obligatoriedad real se valida
en el service, no en el esquema.

**Herencia JPA (`@Inheritance(JOINED)`, `Student extends User`).** Se descarta: clava
el rol en el tipo Java (un usuario nunca podría cambiar de rol), genera SQL
polimórfico poco predecible, y rompe el patrón CRUD por paquete que sigue todo el
repo. La relación real es composición — una cuenta *tiene* un perfil — y ya está
implementada así vía PK compartida.

**`admin_profile`.** El ADMIN queda sin tabla de perfil: su identidad es el email.
El disparador que obligaría a crearla es la **auditoría con nombre humano**:
`Vacancy` ya guarda `reviewed_at` y `admin_comment` pero **no `reviewed_by`**. Cuando
se agregue esa trazabilidad y la UI quiera mostrar "Rechazada por X", se reevalúa.
Agregarla después es una migración aditiva.

Dos reglas que aplican desde ya para cuando exista `reviewed_by`: guardar el
`user_id` como FK (nunca el nombre copiado, para que el histórico sobreviva a un
cambio de nombre), y **no borrar** usuarios con auditoría asociada.

## Consecuencias

**A favor**

- `"user"` sin columnas nullable: la BD deja de aceptar estados imposibles.
- Desaparecen las validaciones por rol en el service y el `if (role == EMPRESA)` de
  `MeController.java:44-48`, que pasa a devolver `user.getStatus()` sin ramas.
- Se elimina el `linkedin_url` duplicado.
- La pregunta abierta de si la empresa tiene persona de contacto **deja de bloquear**:
  si la respuesta es sí, se agregan `contact_name`/`contact_surname` a `company`; si es
  no, no se hace nada. Ninguno de los dos caminos toca `"user"`.

**En contra**

- Migración con movimiento de datos y backfill (V20).
- Todo listado que muestre nombre de usuario necesita un join.
- Hay que reescribir `UserResponse` / `UserMapper` / `MeResponse` y el flujo de
  `UserRegistrationService`.
- `POST /user` deja de crear el perfil: el front **debe** completar el paso 2. Hay que
  comunicarlo explícitamente al equipo de front.

## Plan de migración (V20)

Una sola migración, en este orden:

1. `ALTER TABLE "user" ADD COLUMN status ... DEFAULT 'PENDIENTE'` + `CHECK`.
2. Backfill: `status = 'APROBADO'` para los `ALUMNO`/`ADMIN` existentes y para las
   empresas con `company.approved = true`; `'PENDIENTE'` para el resto.
3. `ALTER TABLE company ADD COLUMN legal_name VARCHAR(255)`; backfill desde
   `"user".name`; recién ahí `SET NOT NULL`.
4. `ALTER TABLE student_profile ADD COLUMN` name, surname, document_type,
   document_number, phone_number, linkedin_url; backfill desde `"user"`; `SET NOT NULL`
   en los obligatorios.
5. `ALTER TABLE "user" DROP COLUMN` name, surname, phone_number, document_type,
   document_number, linkedin_url; `DROP CONSTRAINT ck_user_document_type`.
6. `ALTER TABLE company DROP COLUMN approved, DROP COLUMN user_id`;
   `ALTER TABLE student_profile DROP COLUMN user_id`.
7. FKs de PK compartida (`fk_company_user`, `fk_student_profile_user`) sobre la PK,
   con `ON DELETE CASCADE`.

El backfill (pasos 2-4) debe correr **antes** de cualquier `SET NOT NULL` y antes de
los `DROP COLUMN`, o se pierden los datos existentes.

**Nota operativa:** el `docker-compose.yml` expone tanto `api-local` como `api-hosted`
en el puerto 8080. Verificar contra qué base se está corriendo antes de aplicar esta
migración — aplicarla por accidente sobre Cloud SQL impacta a todo el equipo.

## Fuera de alcance

- Verificación automática de cédula contra `UniversityRegistry` (motiva el estado
  `PENDIENTE`, pero se implementa después).
- Persona de contacto en `Company` (indefinido; el diseño no depende de la respuesta).
- `admin_profile` y `reviewed_by` (ver Alternativas).
- Auto-login en `POST /user`: queda como decisión aparte, no bloquea este ADR.
