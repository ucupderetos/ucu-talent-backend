# El viaje de una request (cómo viaja un dato por la app)

Cuando alguien usa nuestra API (por ejemplo, pide los datos de un usuario), ese
pedido **no lo resuelve un solo archivo**: pasa por varias "estaciones", cada una
con un trabajo distinto. Al principio confunde ver 4 o 5 archivos para un solo
endpoint. Esta guía te muestra **quién hace qué**, siguiendo un pedido real de
nuestra app.

> Antes de leer esto, es útil tener a mano la guía
> `how_to/migracion-basedatos-flyway.md` (cómo se crean las tablas).

---

## 1. La idea en una imagen

Pensá en un **restaurante**:

```
Cliente  →  Mozo       →  Cocinero    →  Despensa     →  (y de vuelta)
(navegador)  (Controller)   (Service)      (Repository)
```

- El **cliente** pide algo (desde el navegador, Swagger o el frontend).
- El **mozo** (Controller) recibe el pedido y lo pasa a la cocina. No cocina.
- El **cocinero** (Service) prepara el plato: es donde está la lógica.
- La **despensa** (Repository) es de donde se sacan los ingredientes: la base de
  datos.

Y después el plato vuelve por el mismo camino hasta el cliente. Cada estación
**solo habla con la de al lado**. El mozo no entra a la despensa; el cocinero no
atiende al cliente. Eso mantiene todo ordenado.

---

## 2. Las estaciones, una por una

Todo esto vive en `src/main/java/ucu/retojulio2026/talent/user/`.

### 🧑‍🍳 Controller — el mozo (`UserController.java`)

Es la **puerta de entrada**. Recibe el pedido HTTP, ve qué se pide, y se lo
delega al Service. Después arma la respuesta. **No tiene lógica de negocio.**

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getById(@PathVariable String id) {
    User user = userService.getById(id);           // le pasa el pedido al cocinero
    return ResponseEntity.ok(userMapper.toResponse(user)); // arma la respuesta
}
```

- `@GetMapping("/{id}")` → "cuando entre un GET a `/user/algo`, atendé acá".
- `@PathVariable String id` → agarra ese `algo` de la URL y lo mete en `id`.
- Devuelve un `ResponseEntity` → la respuesta + el código HTTP (200 = todo bien).

### 👨‍🍳 Service — el cocinero (`UserService` + `UserServiceImpl.java`)

Es donde vive **la lógica**: qué reglas se aplican, qué se hace con los datos.

Fijate que hay **dos** archivos:

- `UserService` (una *interface*) → la **lista de platos del menú**: dice QUÉ se
  puede pedir, pero no cómo se hace.
- `UserServiceImpl` → la **receta concreta**: el cómo.

```java
@Override
public User getById(String id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "User con id '" + id + "' no encontrado"));
}
```

Traducido: "Buscá el usuario en la despensa. Si no está, avisá que no se
encontró" (eso lanza un error que después se convierte en un 404; ver la guía
`how_to/manejo-de-errores-sin-try-catch.md`).

> **¿Por qué separar interface e implementación?** Para que el Controller dependa
> del *menú* (`UserService`) y no de la *receta*. Es el principio SOLID DIP - Inversion de Dependencia.

### 📦 Repository — la despensa (`UserRepository.java`)

Es el que **habla con la base de datos**. Lo mágico: casi no tiene código.

```java
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}
```

Al escribir `extends JpaRepository`, Spring nos **regala gratis** un montón de
funciones: `findById`, `save`, `deleteById`, `existsById`, `findAll`... No hay
que programarlas. Solo agregamos las búsquedas especiales que necesitamos, como
`findByEmail` — y Spring hasta adivina la consulta a partir del nombre del método.

### 🍽️ Entity — el ingrediente (`User.java`)

Es una clase Java que **representa una fila** de la tabla `user`. Cada campo
(`name`, `email`...) es una columna. La anotación `@Entity` le dice a la app "esto
se corresponde con una tabla".

```java
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    private String userId;
    private String name;
    private String email;
    // ...
}
```

---

## 3. El viaje completo, paso a paso 🚶

Sigamos un pedido real: **"dame el usuario con id `abc123`"**.

```
1. El cliente hace:  GET http://localhost:8080/user/abc123

2. 🧑‍🍳 CONTROLLER (UserController.getById)
   - Spring ve la URL y llama a este método.
   - Saca "abc123" de la URL.
   - Le dice al Service: "conseguime el usuario abc123".

3. 👨‍🍳 SERVICE (UserServiceImpl.getById)
   - Le pide al Repository que busque ese id.
   - Si no existe, lanza el error de "no encontrado".

4. 📦 REPOSITORY (UserRepository.findById)
   - Va a la base de datos (tabla "user") y busca la fila.
   - Devuelve el usuario (o vacío si no está).

5. 🍽️ La base responde con la fila → se arma un objeto User.

   ⬅️ Y ahora todo vuelve para atrás:

6. 👨‍🍳 El Service recibe el User y se lo devuelve al Controller.

7. 🧑‍🍳 El Controller convierte el User en un UserResponse
   (el DTO, sin la contraseña — ver HOWTO/por-que-usamos-dtos.md)
   y lo devuelve con código 200.

8. El cliente recibe el JSON:
   { "userId": "abc123", "name": "Ana", "email": "ana@mail.com", ... }
```

---

## 4. ¿Por qué tanto ida y vuelta? ¿No es más simple todo junto?

Es una pregunta súper razonable. Al principio parece burocracia. Pero separar en
capas trae ventajas grandes:

- **Cada archivo hace UNA cosa** → es fácil de entender y de arreglar.
- **Podés cambiar una capa sin romper las otras** → si cambia la base de datos,
  tocás solo el Repository.
- **Es más fácil de testear** → podés probar la lógica del Service sin necesitar
  la base de datos real.
- **Todo el equipo escribe igual** → cualquiera entiende el código de otro.

Es el patrón que usan la mayoría de las APIs profesionales. Vale la pena
acostumbrarse.

---

## 5. Resumen para tener siempre presente

| Estación       | Archivo             | Su único trabajo                                 |
| -------------- | ------------------- | ------------------------------------------------ |
| **Controller** | `UserController`    | Recibir el pedido HTTP y devolver la respuesta   |
| **Service**    | `UserServiceImpl`   | La lógica: qué se hace con los datos             |
| **Repository** | `UserRepository`    | Hablar con la base de datos                       |
| **Entity**     | `User`              | Representar una fila de la tabla                   |
| **DTO**        | `UserResponse`      | Lo que se muestra al cliente (sin datos sensibles) |

Regla de oro del orden: **el pedido baja Controller → Service → Repository → base
de datos, y la respuesta sube por el mismo camino.** Cada capa solo habla con su
vecina.

---

¿Querés crear tu propio endpoint que recorra todo este camino? Seguí la guía
`how_to/crear-un-endpoint-nuevo.md`.
