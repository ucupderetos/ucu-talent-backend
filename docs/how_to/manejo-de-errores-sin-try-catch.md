# Cómo manejar errores sin llenar todo de try-catch

Cuando algo sale mal en la API (un usuario que no existe, un dato inválido), hay
que responderle al cliente con un error claro y el código correcto (como 404
"no encontrado"). La forma **intuitiva** de hacerlo es poner `try-catch` en cada
método del Controller... pero en este proyecto lo hacemos **distinto**.
Esta guía te explica cómo, y por qué.

---

## 1. La forma "intuitiva" (que NO usamos)

Se suele escribir algo así en el Controller:

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getById(@PathVariable String id) {
    try {
        User user = userService.getById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
    } catch (Exception e) {
        return ResponseEntity.notFound().build();   // 😬
    }
}
```

Parece razonable, pero tiene **tres problemas serios**:

1. **`catch (Exception e)` atrapa TODO.** Si falla la base de datos, o hay un bug,
   o cualquier otra cosa, igual responde "404 no encontrado". Le miente al cliente y **esconde los errores de verdad**.

2. **Hay que repetirlo en cada método.** Todos los endpoints terminan con el mismo
   `try-catch` copiado y pegado. Aburrido y fácil de equivocarse.

3. **La respuesta queda pobre.** `notFound().build()` devuelve un 404 vacío, sin
   explicar qué pasó.

---

## 2. La forma que usamos: lanzar y dejar que alguien más lo atrape

La idea es **separar responsabilidades**:

- El **Service**, si algo está mal, **lanza una excepción** (un "error con
  nombre") y se olvida.
- Un **único lugar central** atrapa esas excepciones y las convierte en la
  respuesta HTTP correcta.

Así el Controller queda **limpio, sin un solo `try-catch`**:

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getById(@PathVariable String id) {
    User user = userService.getById(id);
    return ResponseEntity.ok(userMapper.toResponse(user));
}
```

¿Y dónde está el manejo del error? Ya lo vas a ver. Primero, el Service.

---

## 3. El Service lanza la excepción

Mirá el `UserServiceImpl` real:

```java
@Override
public User getById(String id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "User con id '" + id + "' no encontrado"));
}
```

Traducción: "buscá el usuario; **si no está, lanzá** un
`ResourceNotFoundException` con este mensaje". El Service **no se preocupa** por
convertir eso en un 404. Solo dice "esto no existe" y sigue.

`ResourceNotFoundException` es una clase nuestra (está en `common/`) que
simplemente significa "no encontré el recurso pedido".

---

## 4. El lugar central que atrapa todo: `GlobalExceptionHandler`

Acá está la magia. En `common/GlobalExceptionHandler.java` hay una clase especial
que **atrapa las excepciones de TODA la app** y las convierte en respuestas HTTP:

```java
@RestControllerAdvice   // "yo manejo los errores de todos los controllers"
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)  // cuando alguien lance ESTO...
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        // ...respondé con un 404 y el mensaje
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
```

Cómo se conecta todo:

- `@RestControllerAdvice` → le dice a Spring "esta clase se encarga de los errores
  de todos los controllers".
- `@ExceptionHandler(ResourceNotFoundException.class)` → "cuando en cualquier
  lado se lance un `ResourceNotFoundException`, ejecutá este método".
- El método devuelve un **404 con el mensaje** que puso el Service.

Y no solo maneja el "no encontrado". El mismo archivo maneja, por ejemplo, los
**errores de validación** (cuando el cliente manda un email inválido), devolviendo
un 400 con el detalle de qué campo falló. Todo el manejo de errores está **en un
solo lugar**.

---

## 5. El viaje de un error, paso a paso

Veamos qué pasa cuando alguien pide un usuario que no existe:

```
1. Cliente:  GET /user/noexiste

2. 🧑‍🍳 Controller → le pide el usuario al Service (sin try-catch).

3. 👨‍🍳 Service → no lo encuentra → LANZA ResourceNotFoundException.
   (el Controller ni se entera, la excepción "vuela" hacia arriba)

4. 🛡️ GlobalExceptionHandler → atrapa esa excepción
   → la convierte en una respuesta 404 con el mensaje.

5. Cliente recibe:
   Código 404, y un JSON:
   { "status": 404, "detail": "User con id 'noexiste' no encontrado" }
```

El Controller nunca supo del error. Lo lanzó el Service y lo resolvió el handler
central. Cada uno hizo solo su parte.

---

## 6. Por qué esto es mejor

| Con try-catch en cada método | Con excepciones + handler central |
| ---------------------------- | --------------------------------- |
| Código repetido en todos lados | El Controller queda limpio |
| `catch (Exception)` esconde bugs | Cada error se maneja según su tipo |
| Respuestas pobres e inconsistentes | Respuestas uniformes y con detalle |
| Difícil de mantener | Un solo lugar para cambiar todo |

---

## 7. Qué tenés que hacer vos (la regla práctica)

Cuando programes, seguí esto:

- ✅ **En el Service**, si algo está mal, **lanzá una excepción** con nombre
  (`throw new ResourceNotFoundException("...")`). No devuelvas `null`.
- ✅ **En el Controller**, escribí el camino feliz y **no pongas `try-catch`**.
  Dejá que la excepción "vuele".
- ✅ Si necesitás un **tipo de error nuevo** que el handler todavía no maneja,
  pedí ayuda para agregarlo al `GlobalExceptionHandler` (en vez de meter un
  `try-catch`).

🚫 **Si te encontrás escribiendo `try-catch` en un Controller, pará y preguntá.**
Casi siempre hay una forma mejor usando el handler central.

---

Para ver dónde encaja esto en el recorrido completo de un pedido, mirá
`how_to/viaje-de-una-request.md`.
