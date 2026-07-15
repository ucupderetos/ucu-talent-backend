# ADR - 0001: Arquitectura de Servicio

**Status:** accepted
**Date:** 2026-07-14

## Contexto

Se necesita elegir la arquitectura de despliegue del/los servicios para empezar
a trabajar. El proyecto tiene una duración de 3 semanas y se compone por un equipo 
de alumnos que recién empieza a construir servicios web.

Las principales restricciones son:

- **Tiempo acotado (3 semanas)**: hay que priorizar velocidad de desarrollo.
- **Curva de aprendizaje**: el equipo prioriza entregar por sobre aprender a
  operar infraestructura distribuida.
- **Dominio moderado**: gestión de talento (usuarios, autenticación y recursos
  asociados), sin necesidades de escalado independiente por módulo en esta etapa.

## Decision

Se eligió una arquitectura de **Monolito** (un único servicio desplegable con
una sola base de datos), ya que es más rápida de realizar para un proyecto de 3
semanas y tiene menos curva de aprendizaje para alumnos que recién empiezan a
trabajar en servicios web.

Internamente el monolito se organiza en capas y por feature (ver
[ADR-0002](0002-arquitectura-api-rest.md)), de forma que si en el futuro fuera
necesario, un módulo pueda extraerse a un servicio independiente con un costo
razonable.

### Concecuencias Positivas

- Todos los datos se encuentran en una única base de datos: transacciones
  simples y consistencia fuerte sin coordinación distribuida.
- Sin problemas del Teorema de Brewer (CAP) ni de las falacias de la computación
  distribuida (latencia de red, fallos parciales, etc.).
- Un solo artefacto para construir, testear y desplegar: menos superficie
  operativa (un `Dockerfile`, un `docker-compose.yml`).
- Depuración y trazabilidad más simples al no haber saltos entre servicios.

### Consecuencias negativas

- No hay posibilidad de escalar servicios/módulos individuales como en
  microservicios; se escala el proceso completo.
- El equipo trabaja sobre un único proyecto y debe coordinarse a la par para no
  atrasar el desarrollo ni pisarse en el código.
- Riesgo de acoplamiento entre módulos si no se respetan los límites por feature.

## Opciones Consideradas

- **Monolito** (elegido).
- **Microservicios**.

### Pros y Contass 

#### Monolito

- ✅ Rápido de arrancar y de desplegar; una sola base de datos.
- ✅ Consistencia transaccional fuerte; sin complejidad distribuida.
- ✅ Baja curva de aprendizaje para el equipo.
- ❌ Escalado sólo horizontal del proceso completo.

#### Microservicios

- ✅ Escalado y despliegue independiente por servicio.
- ✅ Límites de dominio explícitos y equipos autónomos.
- ❌ Alta complejidad operativa (red, observabilidad, orquestación).
- ❌ Consistencia eventual y manejo de fallos parciales (CAP).
- ❌ Inviable de dominar y entregar en 3 semanas.
