# ADR - 0004: Migraciones de base de datos con Flyway

**Status:** accepted
**Date:** 2026-07-15

## Contexto

El stack ([ADR-0003](0003-stack-tecnologico.md)) usa **Hibernate / Spring Data
JPA** como ORM sobre **PostgreSQL**. Definir una clase `@Entity` en Java **no
crea la tabla** en Postgres, y cuando la entidad cambia (se agrega una columna,
se cambia un tipo) tampoco actualiza la tabla que ya existe.

Hay que decidir **cómo se crea y evoluciona el esquema de la base de datos** de
forma que:

- Cualquier entorno (la máquina de cada integrante, CI, producción) llegue al
  **mismo esquema exacto**.
- El historial de cómo cambió el esquema quede **versionado en git y revisable**
  en los pull requests, no en la memoria de alguien.
- El equipo (que recién arranca con Spring) tenga un flujo simple y difícil de
  romper.

Sin una herramienta de migraciones quedan dos malas opciones: dejar que
Hibernate genere el esquema solo (`ddl-auto=update`), que hace cambios mágicos e
inconsistentes según el estado previo de cada base y nunca borra columnas
viejas; o escribir `ALTER TABLE` a mano y confiar en aplicarlos en el mismo orden
en cada entorno.

## Decision

Se adopta **Flyway** como herramienta de migraciones, con **migraciones SQL
planas versionadas** en `src/main/resources/db/migration/` bajo la convención
`V<n>__<descripcion>.sql` (p. ej. `V1__create_user_table.sql`).

Flyway corre **automáticamente dentro del arranque de Spring Boot**, antes de que
Hibernate valide las entidades. Se combina con **`spring.jpa.hibernate.ddl-auto=validate`**:
Flyway es la **única fuente de verdad del esquema** y Hibernate queda como
**inspector** que verifica al arrancar que las entidades coincidan con las tablas
(ver detalle en la nota de estudio `learning/flyway-con-hibernate.md`).

Principios de uso:

- **Una migración = un cambio** al esquema (crear tabla, `ALTER`, índice, seed),
  no "una tabla".
- **Las migraciones son incrementales y acumulativas**: cada base aplica solo las
  versiones que le faltan; el estado de una tabla es la suma de todas las
  migraciones que la tocaron.
- **Una migración ya aplicada no se edita nunca**. Toda corrección o cambio se
  hace en una migración **nueva** (`V2`, `V3`, …). Editar una vieja cambia su
  checksum y Flyway detiene el arranque por *checksum mismatch*.

## Consecuencias Positivas

- **Reproducibilidad**: `docker compose up` deja cualquier base en el mismo
  esquema, aplicando la secuencia desde cero o solo lo pendiente.
- **Trazabilidad**: la evolución del esquema vive en `.sql` versionados y se
  revisa en los PR junto al código.
- **Red de seguridad con `validate`**: si una entidad y su tabla divergen (falta
  una migración), la app **no levanta** y avisa en el arranque, en vez de fallar
  en un `INSERT` en producción.
- **Curva suave**: SQL plano es transparente y didáctico para un equipo que
  recién empieza; no hay DSL intermedio que aprender.
- **Control total del DDL**: se escribe el SQL exacto (constraints, `CHECK`,
  tipos), sin depender de lo que "adivine" un ORM.

## Consecuencias Negativas

- **Doble mantenimiento manual**: cada cambio de esquema exige escribir la
  migración **y** actualizar la `@Entity`; Flyway no genera ni lee las entidades.
  Mitigado por `ddl-auto=validate`, que rompe el arranque si se olvida.
- **Sin autogeneración de diffs**: a diferencia de otras herramientas, Flyway (en
  su versión Community con SQL plano) no deriva la migración a partir del modelo;
  se escribe a mano.
- **Disciplina requerida**: hay que respetar la regla de no editar migraciones
  aplicadas; un `.sql` viejo tocado rompe el arranque de todos.
- **Rollback no trivial**: revertir implica escribir una migración nueva que
  deshaga el cambio (no hay `downgrade` como en otras herramientas).

## Opciones Consideradas

- **Flyway con SQL plano** (elegido)
- **`ddl-auto=update` de Hibernate** (sin migraciones, el ORM ajusta el esquema)
- **`ALTER TABLE` manual versionado a mano** (sin herramienta)

### Justificación

- **Flyway sobre `ddl-auto=update`**: `update` hace cambios inconsistentes según
  el estado de cada base, nunca borra estructuras obsoletas y **oculta** las
  desincronizaciones que `validate` justamente saca a la luz. Es aceptable para
  un prototipo descartable, no para un esquema que debe ser reproducible entre
  entornos.
- **Flyway sobre `ALTER` manual**: una herramienta que registra qué se aplicó
  (tabla `flyway_schema_history`) y en qué orden elimina el error humano de
  aplicar scripts fuera de orden o repetidos entre entornos.

## Referencias

- [ADR-0003](0003-stack-tecnologico.md) — stack tecnológico donde se eligió Flyway.
- `src/main/resources/db/migration/V1__create_user_table.sql` — primera migración.
- `src/main/resources/application.properties` — `spring.jpa.hibernate.ddl-auto=validate`.

