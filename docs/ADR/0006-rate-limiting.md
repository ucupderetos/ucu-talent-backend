# ADR - 0006: Rate limiting de login y alta de cuentas

**Status:** accepted
**Date:** 2026-07-23

## Contexto

La API expone dos endpoints sin autenticación: `POST /auth/login` y `POST /user` (alta de
cuenta). Son la superficie de ataque natural de cualquier portal: no hay token que validar
antes de procesarlos, así que cualquiera con la URL puede ejercerlos a la velocidad de la red.

Tres riesgos concretos justifican protegerlos desde el diseño:

- **Fuerza bruta y credential stuffing contra el login.** Sin un tope, probar contraseñas
  contra una cuenta conocida es cuestión de tiempo de CPU.
- **Alta masiva y automatizada de cuentas.** Un script puede llenar el padrón de usuarios
  basura, ensuciar los totales que ve el Admin y, como el alta dispara correos, convertir a la
  aplicación en emisora de mail no solicitado, con el costo reputacional que eso tiene para el
  dominio institucional.
- **Consumo de recursos.** El hash de contraseñas es bcrypt con cost 10, deliberadamente caro:
  cada intento de login quema CPU aunque la contraseña sea incorrecta. Un atacante convierte
  esa protección en un vector de agotamiento.

Hay que decidir entonces **por qué clave se limita**, **qué tan severo es el bloqueo** y **cómo
se le comunica al cliente cuándo puede reintentar**. Dos restricciones condicionan la
respuesta:

- **Los usuarios legítimos comparten IP.** En la UCU el campus sale por la misma NAT, y a eso
  se suman proxies corporativos y CGNAT móvil. La IP no identifica a una persona.

## Decision

Se implementa rate limiting con **bucket de tokens** (Bucket4j) y **bloqueo progresivo**, en
un filtro por endpoint (`LoginRateLimitFilter`, `SignupRateLimitFilter`) sobre una base común
(`AbstractKeyedRateLimitFilter`), con caches y configuración independientes entre features: una
IP penalizada en el alta no ve afectado su límite de login, y viceversa.

**1. Doble clave con severidad asimétrica.**

Cada request se mide contra dos claves a la vez —la IP de origen y el email del body— y cada
una tiene su propio limitador y su propia escala de castigo:

| | Capacidad | Escala de bloqueo |
|---|---|---|
| Signup por IP | 10 / 60s | 1 min → 5 min → 30 min |
| Signup por email | 3 / 60s | 15 min → 1 h → 24 h |
| Login por IP | 20 / 60s | 1 min → 5 min → 30 min |
| Login por email | 5 / 60s | 30 s → 3 min → 15 min |

La asimetría es el corazón de la decisión. La IP es una clave **compartida**: bloquearla
castiga a terceros inocentes, así que su límite es holgado y su bloqueo corto, suficiente para
cortar una ráfaga y que se libere solo. El email identifica a **alguien concreto**: ahí el
castigo puede escalar a horas porque el daño colateral es de a uno.

El bloqueo es progresivo: cada vez que la misma clave vuelve a exceder su límite, el castigo
sube un escalón. Reintentar durante la penitencia no la extiende, pero tampoco la acorta.

**2. El tiempo de reintento viaja como número, no como texto.**

La respuesta `429` lleva el header `Retry-After` y, tomado de la misma variable, un
`retryAfterSeconds` como extensión del `ProblemDetail` (RFC 9457 admite miembros de extensión).
El `detail` describe la causa y no hace ninguna afirmación temporal:

```json
{ "status": 429, "title": "Too Many Requests",
  "detail": "Demasiadas cuentas creadas desde este origen.",
  "retryAfterSeconds": 900 }
```

El formateo ("en 15 minutos", "en 1 día") es responsabilidad del cliente, que lo resuelve con
`Intl.RelativeTimeFormat` en el idioma del usuario. El backend no compone frases con datos
adentro: un dato en prosa no se puede reusar, no se puede traducir y se desincroniza del header
en cuanto alguien edita el string.

**3. Alcance explícito: es *best-effort throttling*, no un control de seguridad.**

El estado (buckets y castigos) vive en memoria, en un cache Caffeine acotado por tamaño y por
tiempo. Con más de una instancia, cada una lleva su propia cuenta. Se asume y se acepta para
esta versión: el objetivo es encarecer el abuso, no garantizar un tope global.

**4. Cada bloqueo deja rastro en el log.**

Toda respuesta `429` emite un `WARN` con formato estable y parseable:

```
rate_limit_blocked endpoint="POST /user" clave=ip ip=203.0.113.7 email=a***@correo.ucu.edu.uy retryAfterSeconds=60
```

`clave` dice qué disparó el bloqueo (`ip`, `email`, `ip+email` o `castigo-vigente` cuando el
request llega durante una penitencia ya activa), que es el dato que permite distinguir un
ataque dirigido a una cuenta de una ráfaga desde un origen.

**El email se enmascara** (`a***@correo.ucu.edu.uy`). El log sirve para reconocer el patrón de
abuso, no para dejar direcciones de alumnos en texto plano en un sistema de logs centralizado
al que accede más gente que a la base de datos.

No se expone una métrica de Micrometer: `/actuator/**` está en las rutas públicas, así que
publicar `metrics` dejaría datos operativos accesibles sin autenticación. En Cloud Run el
stdout va a Cloud Logging, donde este renglón se convierte en una métrica basada en logs y en
una alerta sin tocar código.

**5. Todo parametrizado por entorno.**

Capacidades, ventanas y escalas se configuran con variables (`LOGIN_RATE_LIMIT_*`,
`SIGNUP_RATE_LIMIT_*`), incluido un switch para desactivar cada filtro. Ajustar la agresividad
—o apagarla para una corrida de pruebas automatizadas— no requiere tocar código ni reconstruir
la imagen.

## Consecuencias Positivas

- **La fuerza bruta deja de ser viable en la práctica**: cinco intentos por minuto contra un
  email, escalando a 15 minutos de bloqueo, hacen que probar un diccionario cueste mucho tiempo.
- **El alta automatizada masiva se encarece** sin romper el uso legítimo desde una IP
  compartida: una facultad entera puede registrarse, un script no puede correr suelto.
- **El cliente siempre puede mostrar un tiempo correcto**, en su idioma, y es imposible que el
  mensaje y el header se contradigan porque salen del mismo valor.
- **El costo de CPU queda acotado**: el filtro corta antes de llegar a bcrypt, así que un
  atacante no puede forzar hashes ilimitados.
- **Aislamiento entre features**: un problema en el alta no deja a nadie sin poder loguearse.
- **Operable sin deploy**: los límites se ajustan por variable de entorno.

## Consecuencias Negativas

- **Los bloqueos no sobreviven a un deploy ni a un scale-to-zero.** Con `min-instances=0` el
  proceso se apaga solo cuando no hay tráfico, y el estado se pierde.
- **Un límite por IP holgado tolera cierto volumen de automatización** antes de frenar. Es el
  precio de no castigar a usuarios detrás de una NAT compartida.
- **La clave por email es falsificable en el alta**: basta variar el email en cada request para
  evadirla, y ahí solo queda la clave por IP conteniendo. Por eso la de IP no puede ser
  puramente simbólica.
- **La observabilidad depende de la plataforma de logs.** No se expone una métrica propia (ver
  decisión 5), así que armar un panel o una alerta requiere configurar una métrica basada en
  logs en Cloud Monitoring; no sale de la aplicación.

## Opciones Consideradas

- **Doble clave con severidad asimétrica y estado local** (elegido)
- **Una única escala de castigo para ambas claves**
- **Limitar solo por IP**
- **Estado compartido** (Redis / Memorystore; Bucket4j ofrece backend distribuido)
- **Componer el mensaje temporal en el backend** (`"Reintenta en " + humanizar(segundos)`)
- **Persistir los bloqueos en la tabla `audit_log`**
- **CAPTCHA o verificación de email** en el alta

### Justificación

- **Sobre una escala única**: obliga a elegir entre proteger poco o castigar a inocentes. Una
  severidad que es razonable para un email identificado es abusiva para una IP compartida, y a
  la inversa. Dos limitadores cuestan una instancia más y eliminan el compromiso.
- **Sobre limitar solo por IP**: es la implementación más común y la más débil. Deja
  desprotegida la cuenta concreta —el atacante rota IPs— y concentra todo el daño colateral en
  la clave menos precisa.
- **Sobre el estado compartido**: es la solución correcta si el rate limiting llega a ser un
  control de seguridad con garantías. Se descartó por alcance: agrega una dependencia de
  infraestructura y un punto de falla nuevo. Con castigos medidos en minutos, la inconsistencia
  entre instancias tiene consecuencias acotadas.
- **Sobre componer el mensaje en Java**: funciona, pero deja el idioma hardcodeado en el
  backend y dos representaciones del mismo dato que mantener sincronizadas. El número es un
  dato; el formato es presentación, y pertenece a la capa que conoce al usuario.
- **Sobre persistir los bloqueos en `audit_log`**: se descartó por tres motivos. Primero, el
  `429` se emite **antes** de la autenticación, en la cadena de filtros pública: no hay
  `SecurityContext`, así que `actorUserId`, `actorEmail` y `actorRole` —las tres columnas que
  le dan sentido a la tabla— quedarían siempre en null. Segundo, y decisivo: un `INSERT` por
  request bloqueado **amplifica el ataque**. El rate limiting existe para descartar carga
  barata; convertir cada request rechazado en una transacción con `fsync` le da al atacante una
  palanca para pegarle a la base a través del mismo mecanismo que debería protegerla. Tercero,
  retención: los logs vencen solos según la política de la plataforma, mientras que la tabla
  acumula IPs y emails de gente que ni siquiera llegó a autenticarse hasta que alguien la
  limpie a mano.

  Si en el futuro se quiere rastro en base, lo que corresponde persistir **no es cada bloqueo
  sino la apertura de cada castigo** —la transición a "esta clave queda bloqueada N minutos"—,
  que ocurre una vez por penitencia y no una vez por request, y cuyo volumen queda acotado
  incluso bajo ataque. Se haría publicando un evento de dominio consumido con
  `@TransactionalEventListener(AFTER_COMMIT)`, que es la convención del proyecto para efectos
  secundarios.
- **Sobre CAPTCHA y verificación de email**: son defensas de otra naturaleza y más efectivas
  contra el alta masiva —una cuenta sin confirmar no sirve para nada, así que crearla en masa
  deja de tener sentido. Quedan fuera del alcance del SRS. Conviene tenerlo presente: **el rate
  limiting encarece el abuso, no lo vuelve inútil.**

## Pendiente

- Si el rate limiting pasa a ser un control de seguridad con garantías y no una barrera de
  costo, el estado tiene que ser compartido. Mientras tanto, la afirmación correcta es "hasta N
  por minuto **por instancia**", no "N por minuto".
- Crear la métrica basada en logs y la alerta en Cloud Monitoring sobre el renglón
  `rate_limit_blocked`, para ajustar los valores con datos en vez de intuición.

## Referencias

- `src/main/java/ucu/retojulio2026/talent/auth/AbstractKeyedRateLimitFilter.java` — doble limitador, respuesta `429` y log del bloqueo.
- `src/main/java/ucu/retojulio2026/talent/auth/EscalatingKeyRateLimiter.java` — buckets de tokens y bloqueo progresivo.
- `src/main/java/ucu/retojulio2026/talent/auth/LoginRateLimitFilter.java` · `SignupRateLimitFilter.java` — configuración de cada endpoint protegido.
- `src/main/resources/application.properties` — bloques `security.rate-limit.login.*` y `security.rate-limit.signup.*`.
- `src/main/java/ucu/retojulio2026/talent/config/SecurityConfig.java` — `PUBLIC_MATCHER` (por qué no se expone `/actuator/metrics`) y registro de los filtros.
- `.github/workflows/ci-cd.yaml` — `--min-instances=0 --max-instances=2`.
- [ADR-0005](0005-auditoria.md) — auditoría en base de datos, descartada acá como destino de los bloqueos.
- RFC 9110 §10.2.3 (`Retry-After`) y RFC 9457 (extensiones de `ProblemDetail`).

