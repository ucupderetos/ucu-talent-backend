# Cómo crear un endpoint nuevo de punta a punta (receta paso a paso)

Esta es la guía práctica para cuando tengas que agregar una funcionalidad nueva a
la API. La idea es que la sigas **como una receta de cocina**: paso por paso, sin
tener que adivinar nada. Vamos a imaginar que queremos agregar una nueva cosa que
la app guarde (por ejemplo, una **vacante**), pero los pasos sirven para
cualquier entidad.

> **Antes de empezar**, leé estas dos guías para entender el mapa:
> - `HOWTO/viaje-de-una-request.md` (qué hace cada capa)
> - `HOWTO/migracion-basedatos-flyway.md` (cómo se crean las tablas)
>
> Y el mejor truco: **copiá los archivos del módulo `user/` como plantilla.** Ya
> tienen todo bien hecho; solo cambiás los nombres.

---

## Los ingredientes (qué archivos vas a crear)

Para un endpoint completo vas a tocar más o menos estos archivos, **de abajo
hacia arriba** (de la base de datos hacia el cliente):

```
1. Migración .sql   → crea la tabla en la base de datos
2. Entity           → la clase Java que representa la tabla
3. Repository       → habla con la base de datos
4. DTOs             → qué datos entran y qué datos salen
5. Mapper           → traduce entre Entity y DTOs
6. Service          → la lógica (interface + implementación)
7. Controller       → los endpoints (las URLs)
```

Parece mucho, pero cada uno es corto. Vamos uno por uno.

---

## Paso 1 — La migración (crear la tabla) 🗄️

En `src/main/resources/db/migration/` creá el archivo con el número siguiente al
último que exista (ver la guía de Flyway). Por ejemplo `V2__create_vacancy_table.sql`:

```sql
CREATE TABLE vacancy (
    vacancy_id   VARCHAR(12)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  VARCHAR(500) NOT NULL,

    CONSTRAINT pk_vacancy PRIMARY KEY (vacancy_id)
);
```

> Recordá: **nunca edites una migración vieja**, siempre creás una nueva.

---

## Paso 2 — La Entity (la clase de la tabla) 🍽️

Creá una carpeta para tu módulo (ej. `vacancy/`) y adentro la clase. Copiá
`user/User.java` y adaptala. Lo importante: los campos deben coincidir con las
columnas de la migración.

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vacancy")
public class Vacancy {

    @Id
    @Column(name = "vacancy_id", length = 12, updatable = false, nullable = false)
    private String vacancyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    // Genera el id solo, igual que en User
    @PrePersist
    protected void assignId() {
        if (this.vacancyId == null) {
            this.vacancyId = NanoIdGenerator.generate();
        }
    }
}
```

> ⚠️ **Ojo:** los campos de la Entity tienen que coincidir con la migración. Si
> no coinciden, **la app no arranca** y te avisa (es a propósito).

---

## Paso 3 — El Repository (hablar con la base) 📦

Este es el más corto de todos. Copiá `user/UserRepository.java`:

```java
@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, String> {
    // findById, save, deleteById, existsById... vienen gratis.
    // Agregá acá solo búsquedas especiales si las necesitás.
}
```

Con solo escribir eso ya tenés guardar, buscar por id, borrar, etc. **Magia de
Spring.**

---

## Paso 4 — Los DTOs (qué entra y qué sale) 📄

Un **DTO** es una clase que define exactamente qué datos viajan hacia/desde el
cliente. **Nunca exponemos la Entity directa** (ver
`how_to/por-que-usamos-dtos.md`). Normalmente vas a tener dos:

**El de entrada** (lo que el cliente manda para crear), copiá
`user/dto/CreateUserRequest.java`:

```java
public record CreateVacancyRequest(
        @NotBlank(message = "El título es obligatorio")
        String title,

        @NotBlank(message = "La descripción es obligatoria")
        String description
) {}
```

Esas anotaciones (`@NotBlank`, etc.) son **validaciones**: si el cliente manda
algo vacío, la API lo rechaza sola con un error claro.

**El de salida** (lo que la API devuelve), copiá `user/dto/UserResponse.java`:

```java
public record VacancyResponse(
        String vacancyId,
        String title,
        String description
) {}
```

---

## Paso 5 — El Mapper (traductor entre Entity y DTOs) 🔄

En lugar de copiar datos campo por campo a mano, usamos **MapStruct**, que lo hace
solo. Copiá `user/dto/UserMapper.java`:

```java
@Mapper(componentModel = "spring")
public interface VacancyMapper {

    @Mapping(target = "vacancyId", ignore = true) // el id lo genera la Entity
    Vacancy toEntity(CreateVacancyRequest request);

    VacancyResponse toResponse(Vacancy vacancy);
}
```

No escribís la lógica de traducción: MapStruct la genera al compilar mirando los
nombres de los campos.

---

## Paso 6 — El Service (la lógica) 

Van **dos archivos**: la interface (el menú) y la implementación (la receta).

**La interface** (`VacancyService.java`), copiá `user/UserService.java`:

```java
public interface VacancyService {
    Vacancy create(CreateVacancyRequest request);
    Vacancy getById(String id);
    void delete(String id);
}
```

**La implementación** (`VacancyServiceImpl.java`), copiá `user/UserServiceImpl.java`:

```java
@Service
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepository;
    private final VacancyMapper vacancyMapper;

    public VacancyServiceImpl(VacancyRepository vacancyRepository, VacancyMapper vacancyMapper) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyMapper = vacancyMapper;
    }

    @Override
    public Vacancy create(CreateVacancyRequest request) {
        Vacancy vacancy = vacancyMapper.toEntity(request);
        return vacancyRepository.save(vacancy);
    }

    @Override
    public Vacancy getById(String id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vacancy con id '" + id + "' no encontrada"));
    }

    @Override
    public void delete(String id) {
        if (!vacancyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vacancy con id '" + id + "' no encontrada");
        }
        vacancyRepository.deleteById(id);
    }
}
```

> Fijate cómo, si algo no existe, **lanzamos una excepción** en vez de devolver
> `null` o usar `try-catch`. Por qué, en `how_to/manejo-de-errores-sin-try-catch.md`.

---

## Paso 7 — El Controller (las URLs) 🧑

La puerta de entrada. Copiá `user/UserController.java`:

```java
@RestController
@RequestMapping("/vacancy")
public class VacancyController {

    private final VacancyService vacancyService;
    private final VacancyMapper vacancyMapper;

    public VacancyController(VacancyService vacancyService, VacancyMapper vacancyMapper) {
        this.vacancyService = vacancyService;
        this.vacancyMapper = vacancyMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacancyResponse> getById(@PathVariable String id) {
        Vacancy vacancy = vacancyService.getById(id);
        return ResponseEntity.ok(vacancyMapper.toResponse(vacancy));
    }

    @PostMapping
    public ResponseEntity<VacancyResponse> create(@Valid @RequestBody CreateVacancyRequest request) {
        Vacancy created = vacancyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyMapper.toResponse(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        vacancyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Paso 8 — Probarlo ✅

1. Arrancá el proyecto (`docker compose up` o `./mvnw spring-boot:run`).
2. Abrí Swagger: **http://localhost:8080/docs**
3. Vas a ver tu grupo nuevo de endpoints (`/vacancy`). Probá crear uno con el
   botón "Try it out" y después buscarlo por su id.

Si algo falla al arrancar, mirá `HOWTO/levantar-el-proyecto-y-errores-comunes.md`.

---

## Checklist rápido para pegar al lado del monitor 📋

```
[ ] 1. Migración .sql nueva (número siguiente, nunca editar una vieja)
[ ] 2. Entity (campos = columnas de la migración)
[ ] 3. Repository (extends JpaRepository)
[ ] 4. DTOs: uno de entrada (con validaciones) y uno de salida
[ ] 5. Mapper (MapStruct)
[ ] 6. Service: interface + implementación (@Service)
[ ] 7. Controller (@RestController + @RequestMapping)
[ ] 8. Probar en Swagger (/docs)
```

**Consejo final:** no arranques de cero. Copiá el módulo `user/` completo,
renombrá todo a tu entidad, y adaptá. Es la forma más rápida y segura de no
olvidarte nada.
