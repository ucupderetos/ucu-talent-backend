# ADR - 0002: Arquitectura de la API REST

**Status:** accepted
**Date:** 2026-07-14

## Contexto

Dentro del monolito ([ADR-0001](0001-arquitectura-servicio.md)) hay que definir
**cómo se organiza el código internamente** y **qué convenciones sigue la API
REST**, de forma que el equipo trabaje de manera uniforme y el proyecto se pueda
mantener y extender durante las 3 semanas.

Se necesita decidir:

- Cómo estructurar los paquetes y las responsabilidades.
- Cómo exponer los datos hacia el cliente sin filtrar el modelo de persistencia.
- Convenciones de recursos, verbos, códigos de estado y manejo de errores.

## Decision

Se adopta una **arquitectura en capas (Layered)** organizada **por feature**
(package-by-feature), simple y didáctica, alineada con el monolito.

### Organización del código

Cada feature vive en su propio paquete bajo `ucu.retojulio2026.talent`
(`auth`, `user`, `config`, `common`), con las capas:

- **Controller** (`@RestController`): expone los endpoints, valida el request
  (`@Valid`) y traduce a códigos HTTP. No contiene lógica de negocio.
- **Service** (interfaz + `...Impl`): lógica de negocio y transacciones.
- **Repository** (`Spring Data JPA`): acceso a datos.
- **Entity**: modelo de persistencia (JPA), **nunca** expuesto directamente.
- **DTO** (`dto/`): objetos de entrada/salida. El mapeo Entity ↔ DTO se hace con
  **MapStruct** (`UserMapper`).

Lo transversal (excepciones de dominio, manejo global de errores, validadores
custom, generación de IDs) vive en `common`; la configuración en `config`.

### Convenciones REST

- **Recursos** identificados por ruta: `/user`, `/user/{id}`.
- **Verbos HTTP** según semántica: `GET` (consulta), `POST` (creación),
  `DELETE` (baja).
- **Códigos de estado** correctos: `200 OK`, `201 Created` en alta,
  `204 No Content` en baja, `4xx` para errores del cliente.
- **DTOs explícitos** en entrada y salida: nunca se serializa la entity JPA.
- **Errores** en formato **Problem Details (RFC 7807)** vía
  `ProblemDetail` (`spring.mvc.problemdetails.enabled=true`) y un
  `GlobalExceptionHandler` centralizado (`@RestControllerAdvice`).
- **Validación** declarativa con Jakarta Bean Validation en los DTOs de request.
- **Documentación** OpenAPI/Swagger autogenerada con springdoc
  (`/docs` UI, `/api-docs` JSON).

### Concecuencias Positivas

- Estructura simple y predecible: fácil de aprender y de ubicar el código.
- Los DTOs desacoplan el contrato público del modelo de datos: se puede
  refactorizar la persistencia sin romper la API.
- Respuestas de error uniformes y estándar (RFC 7807) para todos los endpoints.
- Package-by-feature deja límites de módulo claros, facilitando una eventual
  extracción a otro servicio.

### Concecuencias Negativas

- La separación en capas y DTOs agrega algo de boilerplate (se mitiga con
  MapStruct y Lombok).
- Al ser un monolito en capas, la disciplina de mantener los límites entre
  features depende del equipo; sin cuidado puede aparecer acoplamiento.

## Opciones Consideradas

- **Arquitectura en capas por feature** (elegida): simple, didáctica y ordenada.
- **Arquitectura hexagonal / DDD**: límites más estrictos, pero demasiado peso y
  curva de aprendizaje para un proyecto de 3 semanas.
- **Sin capas (lógica en el controller)**: más rápido de escribir al inicio,
  pero mezcla responsabilidades y no escala en mantenibilidad.
