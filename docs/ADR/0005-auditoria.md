# ADR - 0005: Auditoría de acciones administrativas

**Status:** accepted
**Date:** 2026-07-20

## Contexto

El Admin UCU tiene poder unilateral sobre datos que no le pertenecen: aprueba y rechaza
cuentas de alumnos y empresas, y modera puestos cambiándoles el estado. Son acciones que
afectan a terceros, que no son reversibles desde la vista del afectado y sobre las que
razonablemente puede haber una disputa posterior: *"¿quién rechazó mi empresa y cuándo?"*.

Sin un registro, la única evidencia de esas acciones es el estado final en la base: se ve que
una cuenta quedó `RECHAZADO`, pero no quién la rechazó, cuándo, ni con qué datos venía la
request. Si además el usuario administrador se borra, se pierde hasta la posibilidad de
reconstruirlo.

Hay que decidir entonces **qué se registra**, **cómo se captura sin ensuciar la lógica de
negocio**, **dónde vive el registro** y **quién puede leerlo**. Tres restricciones dan forma a
la respuesta:

- **La captura no puede acoplarse a cada caso de uso.** Si auditar implica agregar líneas
  dentro de cada método de service, la auditoría se olvida en el próximo método que alguien
  escriba, y el código de negocio queda mezclado con código de trazabilidad.
- **Auditar no puede romper la operación.** Un fallo al registrar no debe abortar una
  aprobación que el Admin ya ejecutó.
- **El registro tiene que sobrevivir a los datos que describe.** Si el actor o la entidad se
  borran, el rastro pierde sentido si depende de ellos por integridad referencial.

## Decision

Se implementa una auditoría **declarativa por anotación**, capturada con **Spring AOP** y
persistida en una tabla propia `audit_log`.

**1. `@Auditable` sobre el método de service, un aspecto lo intercepta.**

```java
@Auditable(module = "VACANCY", action = "VACANCY_STATUS_UPDATE", entityId = "#id")
public Vacancy updateVacancyStatusAdmin(String id, String adminId, UpdateVacancyStatusAdminRequest request)
```

`AuditAspect` intercepta con un `@Around`, ejecuta el método y registra el resultado. El código
de negocio no sabe que está siendo auditado: marcar un método nuevo es una línea.

`entityId` acepta una expresión SpEL evaluada contra los parámetros por nombre (`#id`) o contra
el valor devuelto (`#result.vacancyId`), para poder identificar la entidad afectada incluso
cuando su id se genera dentro del método.

**2. Solo se auditan las acciones ejecutadas por un ADMIN.**

Si el actor resuelto no existe o no tiene rol `ADMIN`, el aspecto ejecuta el método y no hace
nada más. El objetivo de la auditoría es el poder administrativo sobre datos ajenos, no la
actividad de cada usuario sobre lo propio. Como efecto secundario, el costo es cero para el
tráfico de alumnos y empresas, que es la mayoría.

**3. El actor se resuelve en el hilo del request; la escritura es asíncrona.**

`SecurityContextHolder` es un `ThreadLocal`, así que el actor se resuelve **antes** del
`proceed()`, dentro del hilo que atiende la request. La persistencia corre después en el pool
`taskExecutor` (`@Async`), donde ya no hay `SecurityContext`. Invertir ese orden dejaría todos
los registros sin actor.

Un fallo al persistir se atrapa y se loguea: **la auditoría nunca aborta la operación de
negocio**.

**4. Se registra el resultado, el rastro y el payload saneado.**

Cada registro guarda `actorUserId`, `actorEmail`, `actorRole`, `module`, `action`, `entityId`,
`outcome` (`SUCCESS` | `ERROR`), un `message`, un `traceId` y el `detail`. Se auditan **las dos
salidas**: si el método lanza, se registra el `ERROR` y la excepción se vuelve a propagar.

El `traceId` se toma del MDC y correlaciona el registro con las líneas de log de esa misma
request.

El `detail` es la serialización de los argumentos del método, con los campos sensibles
(`password`, `passwordHash`) removidos **recursivamente**, incluso anidados dentro de mapas y
listas.

**5. La tabla no tiene FK al usuario, a propósito.**

`audit_log` guarda `actor_email` y `actor_role` **congelados** al momento del hecho, y no
declara `FOREIGN KEY` contra `"user"`. Si el administrador se borra, el registro sigue diciendo
quién hizo qué. Un rastro que desaparece con el actor no es un rastro.

Hay índices por `actor_user_id`, por `(module, action)` y por `created_at`, que son los tres
ejes por los que se consulta.

**6. La API es de solo lectura y solo para ADMIN.**

`GET /audit` paginado, restringido con `hasRole("ADMIN")` en `SecurityConfig`. No existe
endpoint de escritura ni de borrado: desde la API, el registro es *append-only*.

## Consecuencias Positivas

- **Auditar una acción nueva cuesta una línea.** No hay código de trazabilidad mezclado con la
  lógica de negocio, y agregar cobertura no implica tocar la firma de nada.
- **La operación de negocio nunca se cae por la auditoría**, ni se frena esperándola: la
  escritura es asíncrona y sus errores están contenidos.
- **El rastro sobrevive al borrado del actor**, que es justamente cuando más se lo necesita.
- **Los secretos no llegan a la tabla**: el saneado de `password`/`passwordHash` es recursivo,
  no una lista de campos de primer nivel.
- **Correlación con los logs** por `traceId`, sin tener que cruzar timestamps a ojo.
- **Se registran los fallos, no solo los éxitos.** Un intento fallido de moderación queda
  igual de asentado que uno exitoso.

## Consecuencias Negativas

- **La cobertura es acotada y hay que mantenerla a mano.** Hoy están anotadas las dos acciones
  administrativas que afectan a terceros: `AccountFacadeImpl.reviewAccount`
  (`USER` / `ACCOUNT_REVIEW`) y `VacancyServiceImpl.updateVacancyStatusAdmin`
  (`VACANCY` / `VACANCY_STATUS_UPDATE`). La infraestructura no impone la política: cada acción
  administrativa nueva hay que acordarse de anotarla, y nada avisa si no se hizo.
- **Las acciones de ALUMNO y EMPRESA no dejan rastro.** Una empresa que borra un puesto con
  postulantes, o un alumno que elimina su perfil, no generan ningún registro. Es consecuencia
  directa de la decisión 2 y hay que asumirla explícitamente.
- **La auditoría es *best-effort*, no de grado *compliance*.** Al ser asíncrona y con los
  errores atrapados, una operación puede completarse y su registro perderse sin que nadie se
  entere en el momento. Para trazabilidad operativa alcanza; para una auditoría que deba
  probar hechos ante un tercero, no.
- **Punto ciego por self-invocation.** Al ser un proxy de Spring AOP, una llamada interna
  (`this.metodo(...)`) no pasa por el proxy y no se audita. Quien anote un método tiene que
  saberlo.
- **No hay política de retención.** La tabla crece indefinidamente y el `detail` guarda
  payloads completos de request, que pueden contener datos personales. Nada los purga.
- **`GET /audit` no filtra.** Solo devuelve la página más reciente ordenada por fecha; no se
  puede buscar por actor, módulo, entidad ni rango de fechas, aunque los índices para hacerlo
  ya existen.

## Opciones Consideradas

- **AOP declarativo con anotación y tabla propia** (elegido)
- **Llamadas explícitas al servicio de auditoría** dentro de cada caso de uso
- **Hibernate Envers** (versionado automático de entidades)
- **Triggers de base de datos**
- **Solo logs de aplicación**, sin tabla

### Justificación

- **Sobre las llamadas explícitas**: es la opción más simple de entender y la que peor
  envejece. Mezcla trazabilidad con negocio, se olvida en cada método nuevo y obliga a repetir
  el manejo de errores del registro en cada lugar.
- **Sobre Envers**: audita **cambios de entidad**, no **acciones**. Contestaría "este campo
  pasó de A a B", pero no "el Admin X rechazó esta empresa con este comentario y el resultado
  fue exitoso". Además audita todo lo que se toque, sin distinguir quién ni por qué, lo que
  choca con la decisión 2.
- **Sobre los triggers**: viven fuera de la aplicación, no conocen al usuario autenticado —que
  es el dato central del registro— y quedan invisibles en el código para quien lee el service.
  Contradicen además la separación en tres capas del proyecto.
- **Sobre solo logs**: los logs son volátiles, tienen retención acotada por la plataforma y no
  se pueden consultar desde la aplicación. Para un rastro que puede necesitarse meses después
  y que un Admin debe poder mirar desde el panel, hace falta una tabla. Los logs y la tabla son
  complementarios, y por eso el `traceId` los une.

## Pendiente

- Revisar si alguna acción administrativa futura queda sin anotar; no hay chequeo automático
  que lo detecte.
- Filtros en `GET /audit` por actor, módulo, entidad y rango de fechas: los índices ya están.
- Política de retención o purga de `audit_log`.
- Limpieza en `AuditAspect`: la rama de error usa `System.out.println` y `printStackTrace` en
  lugar del logger, y el método `truncatedStackTrace(...)` quedó sin uso (el `detail` del error
  se guarda como `null`).

## Referencias

- `src/main/java/ucu/retojulio2026/talent/audit/Auditable.java` — la anotación y su contrato SpEL.
- `src/main/java/ucu/retojulio2026/talent/audit/AuditAspect.java` — intercepción, resolución del actor y saneado.
- `src/main/java/ucu/retojulio2026/talent/audit/AuditServiceImpl.java` — persistencia asíncrona y contención de errores.
- `src/main/resources/db/migration/V23__create_audit_log_table.sql` — tabla, constraints e índices.
- `src/main/java/ucu/retojulio2026/talent/config/SecurityConfig.java` — `/audit/**` restringido a `ADMIN`.
