# Por qué NO devolvemos la Entity y usamos DTOs

Cuando mires el código de la app vas a notar algo raro al principio: tenemos una
clase `User` (la Entity) que tiene todos los datos del usuario... pero cuando la
API responde, **no devuelve `User`**, devuelve otra clase llamada `UserResponse`.
Y para crear un usuario, tampoco recibe un `User`, recibe un `CreateUserRequest`.

¿Por qué toda esta vuelta? ¿No sería más fácil usar `User` para todo? Esta guía te
lo explica en fácil.

---

## 1. ¿Qué es un DTO?

**DTO** = *Data Transfer Object* = "objeto para transferir datos".

Es una clase simple cuyo único trabajo es **definir qué datos entran o salen de la
API**. En nuestra app son estos dos (en `user/dto/`):

- `CreateUserRequest` → los datos que la API **recibe** para crear un usuario.
- `UserResponse` → los datos que la API **devuelve** de un usuario.

Y la Entity `User` es otra cosa: representa **cómo se guarda el usuario en la base
de datos**.

La idea clave:

> **La Entity es para adentro (la base de datos). El DTO es para afuera (el
> cliente). Son mundos separados a propósito.**

---

## 2. El problema de devolver la Entity directamente

Miremos la Entity `User`. Tiene este campo:

```java
@Column(name = "password_hash", nullable = false)
private String passwordHash;   // la contraseña encriptada
```

Si la API devolviera el `User` completo, **le estaríamos mandando la contraseña
(aunque esté encriptada) al cliente en cada respuesta.** 🚨 Eso es un problema de
seguridad grave.

Ahora mirá el DTO `UserResponse`:

```java
public record UserResponse(
        String userId,
        String name,
        String email,
        Role role,
        LocalDate registeredAt
) {}
```

**No tiene `passwordHash`.** Ese es justamente el punto: el DTO muestra **solo lo
que es seguro y útil mostrar**, y esconde lo demás.

---

## 3. Las 4 razones por las que usamos DTOs

### 🔒 Razón 1: Seguridad (la más importante)

Como vimos, hay datos que **nunca** deben salir de la app: contraseñas, datos
internos, etc. El DTO es un filtro: solo pasa lo que vos elegís.

### 🎭 Razón 2: Separar "lo de adentro" de "lo de afuera"

Si mañana cambiás cómo se guarda el usuario en la base (agregás una columna
interna, renombrás algo), **el cliente no se tiene que enterar**. Mientras el DTO
siga igual, la API responde igual. Sin DTOs, cualquier cambio en la base rompería
lo que ve el cliente.

Es como un **restaurante**: el cliente ve el plato lindo emplatado (el DTO), no la
cocina desordenada de atrás (la Entity y la base).

### ✅ Razón 3: Validar lo que entra

El DTO de entrada (`CreateUserRequest`) tiene las reglas de qué datos son válidos:

```java
@NotBlank(message = "El email es obligatorio")
@Email(message = "El email no tiene un formato valido")
String email
```

Así la API rechaza sola los datos mal formados, **antes** de que lleguen a tocar
la base de datos. La Entity no es el lugar para esas reglas.

### 🎯 Razón 4: Pedir y mostrar exactamente lo necesario

Para **crear** un usuario pedimos la contraseña (en `CreateUserRequest`), pero al
**mostrarlo** no la devolvemos (en `UserResponse`). Con DTOs distintos para
entrada y salida, cada uno tiene justo los campos que corresponden. La Entity, en
cambio, es una sola y tiene todo mezclado.

---

## 4. ¿Y cómo se pasa de uno a otro? El Mapper

Traducir campo por campo entre `User` y sus DTOs sería aburrido y repetitivo. Por
eso usamos un **Mapper** (con una herramienta llamada MapStruct) que lo hace solo:

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(CreateUserRequest request);   // del DTO de entrada → Entity
    UserResponse toResponse(User user);          // de la Entity → DTO de salida
}
```

Vos no escribís la traducción: MapStruct la genera mirando los nombres de los
campos. En el código lo usás así:

```java
// El cliente manda un CreateUserRequest → lo convertimos en User para guardarlo
User created = userService.create(request);
// El User guardado → lo convertimos en UserResponse para devolverlo (sin contraseña)
return ResponseEntity.ok(userMapper.toResponse(created));
```

---

## 5. El flujo completo, con los DTOs marcados

```
CLIENTE                                                        CLIENTE
manda JSON                                                    recibe JSON
   │                                                              ▲
   ▼                                                              │
CreateUserRequest  ──►  User (Entity)  ──►  base de datos        │
   (DTO entrada)         (mapper)              │                  │
                                               ▼                  │
                                         User (Entity)  ──►  UserResponse
                                                            (DTO salida, SIN contraseña)
```

- Entra un **DTO de entrada** → se convierte en **Entity** → se guarda.
- Se lee una **Entity** → se convierte en **DTO de salida** → se devuelve.

---

## 6. Resumen

| | Entity (`User`) | DTO (`UserResponse` / `CreateUserRequest`) |
| --- | --- | --- |
| ¿Para qué es? | Guardar en la base de datos | Comunicarse con el cliente |
| ¿Tiene la contraseña? | Sí (`passwordHash`) | **No** |
| ¿Quién la ve? | Solo la app, por dentro | El cliente, por fuera |

**La regla del proyecto:** un Controller **nunca** devuelve una Entity directa.
Siempre la convierte a un DTO primero. Si ves que alguien devuelve una Entity,
es una señal de alarma. 🚨

---

Para ver cómo encajan los DTOs en el recorrido completo de un pedido, mirá
`how_to/viaje-de-una-request.md`. Para crear los tuyos, `HOWTO/crear-un-endpoint-nuevo.md`.
