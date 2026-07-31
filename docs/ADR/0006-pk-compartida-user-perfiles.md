# ADR - 0005: PK compartida entre `User` y los perfiles

**Status:** accepted
**Date:** 2026-07-15

## Contexto

Los tres roles de la plataforma necesitan datos distintos. Un alumno tiene documento, skills, CV y
formación; una empresa tiene razón social, rubro y sitio web; un admin apenas nombre y apellido. Lo
único que comparten es lo que los hace usuarios: email, contraseña, rol y estado de la cuenta.

Hay que decidir **cómo se modela esa relación entre la identidad y los datos de cada rol**. Tres
restricciones dan forma a la respuesta:

- **La relación es 1 a 0..1.** Un usuario tiene como máximo un perfil, y el rol determina de qué
  tipo. Nunca hay dos, nunca hay uno de otro rol.
- **El registro es en dos pasos.** Primero se crea la cuenta, después el perfil: son dos pantallas
  distintas y el usuario puede abandonar en el medio.
- **La autenticación identifica al usuario, no al perfil.** El token trae un id; con ese id hay que
  poder llegar tanto a la cuenta como a los datos del rol.

## Decision

Cada tabla de perfil usa **como clave primaria el mismo id del usuario dueño**, con una foreign key
a `"user"` y borrado en cascada.

```sql
CREATE TABLE student_profile (
    student_profile_id VARCHAR(12) NOT NULL,   -- ES el user_id
    ...
    CONSTRAINT pk_student_profile PRIMARY KEY (student_profile_id),
    CONSTRAINT fk_student_profile_user FOREIGN KEY (student_profile_id)
        REFERENCES "user"(user_id) ON DELETE CASCADE
);
```

Lo mismo en `company` y en `admin`. No hay columna `user_id` aparte: **la PK ya lo es**.

De esa decisión se derivan tres reglas que se ven en todo el código:

**1. Buscar por usuario es buscar por id.** No hace falta un índice, ni una query por FK, ni un
método distinto. `GET /student-profile?userId={id}` es literalmente `getById(userId)`, y el código
lo dice:

```java
// PK compartida: studentProfileId == userId, asi que buscar por userId es getById.
StudentProfile studentProfile = studentProfileService.getById(userId);
```

**2. El id nunca viene del body.** Como el id del perfil **es** el del usuario autenticado, se toma
del token. Los `Create...Request` de perfil no tienen campo `id`: si lo tuvieran, un usuario podría
crear el perfil de otro.

**3. La unicidad la garantiza la base.** "Un usuario tiene como máximo un perfil" no es una
validación que alguien tenga que recordar escribir: es la PRIMARY KEY. Un segundo intento choca
contra la base antes de tocar la lógica.

Los datos que son de la **cuenta** y no del perfil —`email`, `status`, `registeredAt`— viven solo
en `"user"` y se resuelven aparte cuando hay que devolverlos:

```java
// status, email y registeredAt viven en User, no en StudentProfile (PK compartida) - se pasan aparte.
@Mapping(target = "email", source = "user.email")
StudentProfileResponse toResponse(StudentProfile profile, User user, AccountStatus status);
```

## Consecuencias Positivas

- **Un solo id por persona en todo el sistema.** El `subject` del token sirve para el usuario y
  para su perfil. No hay que traducir de un id a otro en ningún lado, ni arrastrar dos ids por las
  capas.
- **La cardinalidad la impone el motor.** "Como máximo un perfil por usuario" es estructural, no
  una regla de negocio que se pueda olvidar en un endpoint nuevo.
- **Los joins son directos y baratos**: siempre `ON perfil.pk = user.pk`, sobre índices de clave
  primaria en las dos puntas. Se ve en todas las consultas compuestas del proyecto.
- **El borrado se propaga solo.** `ON DELETE CASCADE` se lleva el perfil cuando se borra la cuenta,
  sin código de limpieza.
- **No hay estados imposibles**: no puede existir un perfil huérfano, ni un perfil apuntando a un
  usuario que no existe.

## Consecuencias Negativas

- **Un usuario no puede tener dos roles.** Una persona que sea alumno y a la vez referente de una
  empresa necesita dos cuentas con dos emails. Es una limitación real del modelo, aceptada porque
  el SRS define los roles como excluyentes.
- **El rol y el perfil pueden desincronizarse.** Nada a nivel base impide que un usuario con
  `role = ALUMNO` tenga fila en `company`: la FK apunta a `"user"`, no al rol. La coherencia la
  sostiene la aplicación.
- **Devolver un perfil siempre requiere dos fuentes.** `email` y `status` no están en la tabla del
  perfil, así que cada respuesta se arma con el `User` al lado. Es la razón de que los mappers
  reciban dos parámetros y de que las consultas compuestas siempre incluyan `JOIN User`.
- **El registro tiene un estado intermedio visible.** Entre el paso 1 y el paso 2 existe un usuario
  sin perfil, y el sistema tiene que saber convivir con eso: por eso `GET /me` expone `hasProfile`,
  para que el frontend pueda retomar un registro cortado por la mitad.
- **Coordinar operaciones que tocan usuario y perfil a la vez** no puede vivir en ninguno de los
  dos services sin crear una dependencia circular. Por eso existe `AccountFacade`, el único punto
  que conoce a los cuatro.

## Opciones Consideradas

- **PK compartida con FK y cascada** (elegido)
- **Perfil con PK propia + columna `user_id` UNIQUE**
- **Una sola tabla `user` con todas las columnas de los tres roles**
- **Herencia de JPA** (`SINGLE_TABLE` o `JOINED`)

### Justificación

- **Sobre la PK propia + `user_id` UNIQUE**: funciona, pero agrega un identificador que no aporta
  información —dos ids para la misma persona— y hay que mantener el UNIQUE, un índice extra y la
  traducción entre ids en cada capa. La PK compartida consigue lo mismo con menos piezas.
- **Sobre la tabla única**: obliga a que todas las columnas sean nullable, porque `documentNumber`
  no aplica a una empresa ni `industry` a un alumno. Se pierde la posibilidad de declarar
  `NOT NULL` donde corresponde, y la tabla se vuelve una bolsa donde nada se puede exigir.
- **Sobre la herencia de JPA**: `SINGLE_TABLE` es la tabla única con otro nombre, y `JOINED` genera
  exactamente este esquema pero con el ORM manejando la jerarquía, lo que trae *downcasting*,
  consultas polimórficas y un acoplamiento de tipos que complica algo que en realidad es simple.
  El proyecto además evita las relaciones gestionadas por el ORM ([ADR-0009](0009-fks-como-string-plano.md)),
  así que la herencia habría sido incoherente con el resto.

## Referencias

- `src/main/resources/db/migration/V20__split_user_profile_fields.sql` — la FK con `ON DELETE CASCADE` sobre la PK.
- `src/main/resources/db/migration/V21__create_admin_table.sql` — mismo patrón para `admin`.
- `src/main/java/ucu/retojulio2026/talent/user/AccountFacade.java` — coordinación entre cuenta y perfil.
- `src/main/java/ucu/retojulio2026/talent/studentprofile/StudentProfileController.java` — `getByUserId` resuelto como `getById`.
- `src/main/java/ucu/retojulio2026/talent/auth/dto/MeResponse.java` — `hasProfile`, el estado intermedio del registro.
- [ADR-0007](0007-autenticacion-jwt-en-cookie.md) — el `subject` del token es ese id compartido.
