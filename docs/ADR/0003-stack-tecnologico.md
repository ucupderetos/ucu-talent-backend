# ADR - 0003: Stack tecnológico

**Status:** accepted
**Date:** 2026-07-14

## Contexto

Es necesario elegir el stack para construir la API REST del monolito
([ADR-0001](0001-arquitectura-servicio.md)) con la arquitectura en capas
definida en [ADR-0002](0002-arquitectura-api-rest.md).

Hay que elegir, como mínimo: **lenguaje/framework web, ORM, herramienta de
migraciones, base de datos, seguridad y documentación de la API.**

## Decision

Se eligió **Java 21 + Spring Boot** para la API REST, con:

| Área          | Elección                                   |
| ------------- | ------------------------------------------ |
| Lenguaje      | Java 21                                     |
| Framework     | Spring Boot 4 (Spring Web MVC)              |
| ORM           | Hibernate (vía Spring Data JPA)             |
| Migraciones   | Flyway                                       |
| Base de datos | PostgreSQL                                   |
| Seguridad     | Spring Security + OAuth2 Resource Server (JWT) |
| Mapeo DTO     | MapStruct + Lombok                          |
| Documentación | springdoc-openapi (Swagger UI)             |

Motivos de la elección:

- **Productividad para CRUD + REST** con validación y serialización de primera
  clase (Jakarta Bean Validation, Jackson).
- **Read-heavy con DTOs explícitos**: nunca se expone el modelo ORM directo (ver
  [ADR-0002](0002-arquitectura-api-rest.md)).
- **Encaja con la arquitectura elegida** (Layered: simple y didáctica).
- **Transaccionalidad y concurrencia robustas** de la JVM (`@Transactional`).
- **Ecosistema** ampliamente documentado, útil para un equipo que
  recién empieza.
- 

Detalle de componentes:

- **Spring Boot 4 (Web MVC)**: framework web, inyección de dependencias y
  autoconfiguración.
- **Hibernate / Spring Data JPA**: ORM maduro con control fino; mapea filas SQL
  a entidades Java y genera repositorios a partir de interfaces.
- **Flyway**: migraciones versionadas y reproducibles de la base de datos.
- **PostgreSQL**: motor de base de datos relacional robusto y open source.
- **Spring Security + OAuth2 Resource Server**: autenticación/autorización
  basada en JWT.
- **MapStruct + Lombok**: mapeo Entity ↔ DTO y reducción de boilerplate en
  tiempo de compilación.
- **springdoc-openapi**: documentación OpenAPI/Swagger autogenerada
  (`/docs` y `/api-docs`).

### Concecuencias Positivas

- Velocidad de desarrollo alta para el grueso del servicio (CRUD + endpoints).
- Transaccionalidad y concurrencia robustas (JPA, `@Transactional`).
- Acceso pleno a las features de PostgreSQL cuando el dominio lo pide.
- Migraciones reproducibles y revisables en el repositorio.
- Seguridad y documentación resueltas con componentes estándar del ecosistema.

### Concecuencias Negativas

- Mayor peso y consumo de recursos de la JVM frente a alternativas más livianas.
- Curva de aprendizaje de Spring (anotaciones, autoconfiguración) para un equipo
  que recién arranca.
- Tiempos de arranque y de build más altos que en stacks interpretados.
- Boilerplate inherente a Java/JPA (mitigado con Lombok y MapStruct).

## Opciones Consideradas

- **Framework web**: **Spring Boot** (elegido) · FastAPI · ASP.NET.
- **ORM**: **Hibernate / Spring Data JPA** (elegido) · SQL crudo (JDBC Template).
- **Migraciones**: **Flyway** (elegido) · Liquibase · SQL manual versionado ·
  sin migraciones.
- **Base de datos**: **PostgreSQL** (elegido) · MySQL.

### Justificación 

- **Spring Boot** sobre FastAPI/ASP.NET: el equipo tiene base en Java y prioriza
  la robustez transaccional de la JVM y un ecosistema muy documentado.
- **Hibernate/JPA** sobre JDBC Template: mayor productividad para CRUD.
