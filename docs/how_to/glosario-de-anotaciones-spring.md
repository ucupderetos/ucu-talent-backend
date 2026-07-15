# Glosario de anotaciones de Spring (las @)

En Spring vas a ver un montón de palabras que empiezan con `@`, como
`@RestController` o `@Entity`. Se llaman **anotaciones**. Son como **etiquetas
que le pegás a una clase o a un método para decirle a Spring "che, tratá esto de
tal manera"**. No hacen nada por sí solas: Spring las lee y actúa.

Esta guía es un diccionario para consultar. No hace falta memorizarlo: tenelo a
mano y buscá la que no entiendas. Todos los ejemplos son de nuestra app.

---

## 1. Las que marcan "qué tipo de clase es esto"

Spring necesita saber qué rol cumple cada clase. Estas anotaciones se lo dicen.

| Anotación | Qué significa | Dónde la ves |
| --------- | ------------- | ------------ |
| `@RestController` | "Esta clase atiende pedidos web y devuelve JSON." | `UserController` |
| `@Service` | "Esta clase tiene lógica de negocio." | `UserServiceImpl` |
| `@Repository` | "Esta clase habla con la base de datos." | `UserRepository` |
| `@Configuration` | "Esta clase tiene configuración de la app." | `SecurityConfig` |
| `@Entity` | "Esta clase representa una tabla de la base de datos." | `User` |

> **¿Por qué importan?** Cuando Spring arranca, busca estas etiquetas y crea una
> copia de cada clase para usarla y prestarla donde haga falta. A eso se le llama
> que la clase es un **"bean"** (un objeto que maneja Spring por vos).

---

## 2. Inyección de dependencias (cómo se conectan las clases)

Fijate en el Controller:

```java
public class UserController {
    private final UserService userService;   // necesita un Service para trabajar

    public UserController(UserService userService) {  // lo recibe acá
        this.userService = userService;
    }
}
```

El Controller **necesita** un Service para funcionar. Pero **no lo crea él mismo**
(no hace `new UserServiceImpl()`). En cambio, lo **pide en el constructor**, y
Spring se lo **entrega automáticamente**. A esto se le llama **inyección de dependencias**.

Analogía: es como pedir un Uber. Vos no fabricás el auto; lo pedís y te lo traen.
Spring es el que "trae el auto".

> **Dato:** vas a ver por ahí la anotación `@Autowired` para esto. En nuestro  proyecto usamos el estilo **más recomendado**: pedir las cosas en el constructor (como arriba), que no necesita `@Autowired`. Si ves `@Autowired` en un tutorial, es lo mismo pero a la vieja usanza.

---

## 3. Las que definen los endpoints (las URLs)

Estas van en el Controller y dicen **a qué URL responde cada método**.

| Anotación | Qué hace | Ejemplo |
| --------- | -------- | ------- |
| `@RequestMapping("/user")` | La ruta base de todo el Controller. | Todo cuelga de `/user` |
| `@GetMapping("/{id}")` | Responde a un GET (pedir/leer datos). | `GET /user/abc123` |
| `@PostMapping` | Responde a un POST (crear algo). | `POST /user` |
| `@PutMapping("/{id}")` | Responde a un PUT (actualizar algo). | `PUT /user/abc123` |
| `@DeleteMapping("/{id}")` | Responde a un DELETE (borrar algo). | `DELETE /user/abc123` |

> GET, POST, PUT, DELETE son los **verbos HTTP**: la forma estándar de decir "leer",
> "crear", "actualizar", "borrar". Es una convención que usan todas las APIs.

---

## 4. Las que agarran datos del pedido

Cuando llega un pedido, estas anotaciones sacan los datos de distintos lugares.

| Anotación | De dónde saca el dato | Ejemplo |
| --------- | --------------------- | ------- |
| `@PathVariable` | De la **URL**. | En `/user/abc123`, agarra `abc123` |
| `@RequestParam` | De los **parámetros** después del `?`. | En `/user?email=a@b.com`, agarra el email |
| `@RequestBody` | Del **cuerpo** del pedido (el JSON que manda el cliente). | El JSON para crear un usuario |

Ejemplo real:

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getById(@PathVariable String id) { ... }
//                                            ↑ saca el id de la URL

@PostMapping
public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest request) { ... }
//                                          ↑ convierte el JSON de entrada en un objeto Java
```

---

## 5. Las de validación (revisar que los datos estén bien)

Estas se ponen en los DTOs de entrada y hacen que la API **rechace sola** los
datos mal formados, sin que vos escribas los `if`.

| Anotación | Qué exige |
| --------- | --------- |
| `@NotBlank` | El texto no puede estar vacío. |
| `@NotNull` | El valor no puede faltar. |
| `@Email` | Tiene que tener forma de email. |
| `@Size(min = 8)` | Largo mínimo (ej. contraseña de 8+). |
| `@Valid` | "Revisá todas las validaciones de este objeto." (va en el Controller) |

Ejemplo real (en `CreateUserRequest`):

```java
@NotBlank(message = "El email es obligatorio")
@Email(message = "El email no tiene un formato valido")
String email
```

Y en el Controller, `@Valid` dispara esas revisiones:

```java
public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request)
```

Si el cliente manda un email inválido, la API responde sola con un error 400 y el mensaje que pusiste. **No hace falta programar la revisión.**

---

## 6. Las de la base de datos (en las Entities)

Estas van en las clases `@Entity` y describen cómo se guarda cada campo.

| Anotación | Qué hace |
| --------- | -------- |
| `@Id` | Marca cuál campo es la clave primaria (el identificador único). |
| `@Column` | Detalles de la columna (nombre, largo, si puede ser nulo). |
| `@Table(name = "...")` | El nombre de la tabla en la base. |
| `@Enumerated` | Cómo guardar un enum (ej. el rol como texto "ALUMNO"). |
| `@CreationTimestamp` | Pone la fecha automáticamente al crear el registro. |
| `@PrePersist` | Ejecuta un método justo antes de guardar (ej. generar el id). |

Ejemplo real (en `User`):

```java
@Id
@Column(name = "user_id", length = 12, nullable = false)
private String userId;
```

---

## 7. Lombok (las que te ahorran escribir código)

**Lombok** es una herramienta que genera código repetitivo por vos. Estas van
arriba de una clase:

| Anotación | Qué te genera solo |
| --------- | ------------------ |
| `@Getter` | Los métodos para leer los campos (`getName()`, etc.). |
| `@Setter` | Los métodos para cambiar los campos (`setName()`, etc.). |
| `@NoArgsConstructor` | Un constructor vacío. |
| `@AllArgsConstructor` | Un constructor con todos los campos. |
| `@ToString` | Un método para imprimir el objeto de forma legible. |

Gracias a Lombok, la clase `User` no tiene 40 líneas de getters y setters: los
genera Lombok al compilar. Vos escribís solo los campos.

---

## 8. Cómo usar este glosario

- No lo memorices. **Consultalo** cuando veas una `@` que no entiendas.
- Si una anotación no está acá, es buena señal para **preguntar** o buscarla: casi
  siempre significa algo simple.
- Regla mental: **una anotación es una instrucción para Spring/Java, no código que
  se ejecuta como el resto.** Es una etiqueta que alguien más lee por vos.

---

Para ver estas anotaciones "en acción" recorriendo un pedido real, mirá
`docs/how_to/viaje-de-una-request.md`.
