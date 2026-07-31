# ADR - 0005: Autenticación stateless con JWT en cookie HttpOnly

**Status:** accepted
**Date:** 2026-07-17

## Contexto

La API sirve a un frontend Next.js que corre en **otro dominio** y expone recursos de tres roles
distintos, donde casi todo endpoint necesita saber **quién** está llamando: un alumno solo puede
ver sus postulaciones, una empresa solo los candidatos de sus puestos, un admin puede moderar.

Hay que decidir **cómo viaja la identidad en cada request** y **dónde vive la sesión**. La decisión
condiciona tres cosas a la vez:

- **Dónde guarda el token el frontend.** Si queda accesible desde JavaScript, cualquier script
  inyectado en la página puede leerlo y robar la sesión.
- **Si el servidor guarda estado de sesión.** Una sesión en memoria obliga a que todas las
  instancias compartan ese estado, o a pegar al usuario a una instancia.
- **Cuánta ceremonia carga el frontend.** Si tiene que leer el token, guardarlo y adjuntarlo a mano
  en cada llamada, cada pantalla nueva es una oportunidad de olvidarse.

Restricciones del contexto: el despliegue es Cloud Run con hasta dos instancias y `min-instances=0`
—el proceso se apaga solo cuando no hay tráfico—, y el frontend está en un dominio distinto al de
la API, así que cualquier cookie viaja **cross-site**.

## Decision

Se adopta **autenticación stateless con JWT firmado, transportado en una cookie `HttpOnly`**.

**1. El servidor no guarda nada de la sesión.**

El token se firma con HMAC-SHA256 usando un secreto de entorno (`JWT_SECRET`). Toda la información
necesaria para autorizar viaja adentro y firmada:

```java
JwtClaimsSet.builder()
        .subject(user.getUserId())          // el id, no el email
        .claim("role", user.getRole().name())
        .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
```

Validar una request es verificar una firma: **cero consultas a la base**, y cualquier instancia
puede atender cualquier request sin conocer a las demás.

**2. El token viaja en cookie `HttpOnly`, no en el header `Authorization`.**

```java
ResponseCookie.from("access_token", token)
        .httpOnly(true)      // JavaScript no lo puede leer
        .secure(true)        // solo por HTTPS
        .sameSite("None")    // el front está en otro dominio
        .path("/")
        .maxAge(Duration.ofMinutes(expirationMinutes))
```

`HttpOnly` es el punto central: un XSS en el frontend **no puede robar la sesión**, porque el token
no es accesible desde JavaScript. Con el token en `localStorage`, sí lo sería.

Como Spring Security espera el token en el header, un `CookieBearerTokenResolver` lo saca de la
cookie y lo entrega como si hubiera venido en `Authorization: Bearer`. El resto del stack de
seguridad funciona sin enterarse.

**3. El id del usuario sale siempre del token, nunca del body ni de la URL.**

El `subject` del JWT es el `userId`. Los endpoints que operan sobre "lo propio" no reciben id:

```java
@GetMapping("/me/detailed")
public ResponseEntity<List<MyApplicationRowResponse>> getMyApplicationsDetailed(
        @AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(service.getMyApplicationsDetailed(jwt.getSubject()));
}
```

No hay forma de pedir las postulaciones de otro alumno: no existe el parámetro donde poner su id.
La clase entera de vulnerabilidad (IDOR) desaparece por construcción, no por una validación que
alguien podría olvidarse de escribir.

**4. Dos cadenas de filtros: pública y autenticada.**

`SecurityConfig` define un `PUBLIC_MATCHER` (login, alta de cuenta, Swagger, health) que corre sin
JWT, y una segunda cadena donde todo lo demás exige token válido. La autorización se expresa en dos
niveles: **por rol** con `hasRole(...)` en la config, y **por dueño** con `AuthorizationGuard`
comparando el `subject` del token contra el dueño del recurso.

El claim `role` se mapea a la authority que espera Spring:

```java
authoritiesConverter.setAuthoritiesClaimName("role");
authoritiesConverter.setAuthorityPrefix("ROLE_");   // ALUMNO -> ROLE_ALUMNO
```

**5. Logout = borrar la cookie.**

No hay lista de tokens revocados. `POST /auth/logout` devuelve la misma cookie con `maxAge(0)`.

## Consecuencias Positivas

- **Un XSS no roba la sesión.** Es la razón principal de la decisión y la que no se consigue con
  el token en `localStorage`.
- **Escala horizontalmente sin infraestructura extra.** No hace falta Redis ni sesiones pegajosas:
  cualquier instancia valida cualquier token, y el scale-to-zero no desloguea a nadie.
- **Validar no cuesta base de datos.** Solo verificar una firma.
- **El frontend no maneja el token.** El browser manda la cookie solo; alcanza con
  `credentials: "include"`. No hay forma de olvidarse de adjuntarla en una pantalla nueva.
- **Elimina una familia entera de bugs de autorización**, porque la identidad no es un dato que el
  cliente pueda elegir.

## Consecuencias Negativas

- **No se puede revocar un token antes de que venza.** Si una cuenta se compromete o un admin es
  dado de baja, su token sigue siendo válido hasta la expiración. Es el costo directo de no tener
  estado. Se mitiga con un vencimiento corto, no se elimina.
- **Un cambio de rol o de estado no tiene efecto hasta el próximo login.** Si el Admin aprueba una
  cuenta, el token viejo sigue diciendo lo que decía. Hoy no molesta porque el `status` no está en
  el token —se consulta contra la base cuando hace falta—, pero el `role` sí.
- **`SameSite=None` es obligatorio y trae condiciones.** Al estar el front en otro dominio, la
  cookie tiene que ser cross-site, lo que **exige `Secure`** y por lo tanto **HTTPS**: en `http://`
  plano el navegador la descarta. Además obliga a `allowCredentials(true)` en CORS, que a su vez
  prohíbe usar `*` como origen permitido: hay que enumerar los dominios.
- **Al ser autenticación por cookie, el navegador la manda sola**, que es justamente el escenario
  de CSRF. Hoy `csrf()` está deshabilitado; la protección real es `SameSite`, no un token
  anti-CSRF. Es una decisión que conviene revisar si algún día se acepta `SameSite=Lax`.
- **El secreto de firma es un único punto de falla.** Quien lo tenga puede emitir tokens válidos
  para cualquier usuario y rol. Vive en `JWT_SECRET`, fuera del repositorio.

## Opciones Consideradas

- **JWT stateless en cookie `HttpOnly`** (elegido)
- **JWT en el header `Authorization`**, guardado por el front en `localStorage`
- **Sesión con estado en el servidor** (`JSESSIONID` + almacén de sesiones)
- **JWT de acceso corto + refresh token**

### Justificación

- **Sobre el token en `localStorage`**: es la opción más común en tutoriales y la más frágil. Todo
  el contenido de `localStorage` es legible por cualquier script que corra en la página; un XSS
  pasa de "defacear la UI" a "robar la sesión de un admin". La cookie `HttpOnly` cierra eso a nivel
  navegador.
- **Sobre la sesión con estado**: resuelve la revocación —la ventaja real que no tenemos—, pero
  exige un almacén compartido entre instancias o afinidad de sesión. Con `min-instances=0` la
  sesión en memoria además se pierde cada vez que el servicio se apaga por falta de tráfico.
  Cambiar una limitación conocida por una dependencia de infraestructura no valía la pena.
- **Sobre el refresh token**: es la respuesta correcta a la revocación —access token de minutos y
  refresh revocable en base— pero duplica el flujo de autenticación y agrega una tabla y un
  endpoint. Para el alcance actual, un token con vencimiento acotado alcanza. Es el camino natural
  si el proyecto sigue.

## Pendiente

- Definir un vencimiento coherente para producción. En local está en 240 minutos (`.env`), y el
  default del código es 60. Cuanto más largo, más pesa no poder revocar.
- Si alguna vez se manejan datos más sensibles, evaluar refresh token con revocación en base.
- Revisar la postura de CSRF si el frontend pasa a compartir dominio con la API.

## Referencias

- `src/main/java/ucu/retojulio2026/talent/config/SecurityConfig.java` — cadenas de filtros, `PUBLIC_MATCHER`, conversión del claim `role`, CORS.
- `src/main/java/ucu/retojulio2026/talent/auth/JwtService.java` — emisión y claims del token.
- `src/main/java/ucu/retojulio2026/talent/auth/CookieBearerTokenResolver.java` — de la cookie al header.
- `src/main/java/ucu/retojulio2026/talent/auth/AuthController.java` — login, logout y atributos de la cookie.
- `src/main/java/ucu/retojulio2026/talent/common/AuthorizationGuard.java` — autorización por dueño.
- [ADR-0006](0006-pk-compartida-user-perfiles.md) — por qué el `subject` es el `userId` y eso alcanza para identificar también al perfil.
- `learning/jwt-stateless-y-auth-nextjs.md` — nota de estudio con el recorrido completo.
