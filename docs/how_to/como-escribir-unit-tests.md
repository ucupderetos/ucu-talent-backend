# Cómo escribir un unit test de reglas de negocio

---

## 1. Qué SÍ y qué NO es esto

✅ **Sí es esto:** instanciar un `Service` a mano, mockear sus dependencias
(`Repository`, otros `Service`, `PasswordEncoder`, lo que sea), y verificar el
comportamiento: "si el email ya existe, tiene que tirar
`DuplicateResourceException`", "el estado no puede retroceder", etc.

🚫 **No es esto:** `@SpringBootTest`, `@DataJpaTest`, H2, o cualquier cosa que
levante un contexto de Spring o hable con una base de datos real. Eso es
**testing de integración**, y está fuera del alcance de esta tanda de tests.


---

## 2. Setup: no hay que instalar nada

El `pom.xml` ya trae todo lo necesario, aunque no lo veas como una dependencia
con nombre obvio. Los starters de test que ya están (`spring-boot-starter-data-jpa-test`,
etc.) arrastran transitivamente:

- **JUnit 5** (`org.junit.jupiter`) — el framework de test.
- **Mockito** (`org.mockito`) — para crear los mocks.
- **AssertJ** (`org.assertj`) — para los `assertThat(...)` más legibles.

---

## 3. Dónde va el archivo

Los tests espejan el paquete de la clase que prueban, debajo de `src/test/java`
en vez de `src/main/java`. Ejemplo: si la clase es

```
src/main/java/ucu/retojulio2026/talent/user/UserServiceImpl.java
```

el test va en

```
src/test/java/ucu/retojulio2026/talent/user/UserServiceImplTest.java
```

Mismo paquete, mismo nombre + `Test` al final.

---

## 4. La estructura básica (las 3 anotaciones que importan)

```java
@ExtendWith(MockitoExtension.class)   // 1. Activa Mockito en JUnit5
class UserServiceImplTest {

    @Mock                              // 2. "Creame un doble falso de esta dependencia"
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    // 3. Si preferís crear la clase a mano en cada test, no hace falta @InjectMocks:
    //    `new UserServiceImpl(userRepository, passwordEncoder, userMapper)` alcanza.
}
```

- `@Mock` crea un objeto falso de esa interfaz/clase. Por defecto, todos sus
  métodos devuelven `null` (o `false`/`0`) hasta que vos le digas qué
  devolver con `when(...)`.
- Instanciás la clase real que estás probando (el `Service`) pasándole los
  mocks por constructor — así se ve clarísimo qué recibe cada test.

---

## 5. Paso a paso con un ejemplo real

Tomemos `UserServiceImpl.create()` (RF-AUT-01: registro de usuario). Mirá el
archivo completo en
`src/test/java/ucu/retojulio2026/talent/user/UserServiceImplTest.java` — acá
el desglose de un caso:

```java
@Test
void registro_falla_si_el_email_ya_existe() {
    // 1. ARRANGE: preparo la clase real y le digo a los mocks qué devolver
    UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
    CreateUserRequest request = new CreateUserRequest("nicogon@ucu.edu.uy", "unaClaveSegura123", Role.ALUMNO);
    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    // 2. ACT + ASSERT: ejecuto y verifico que tire la excepción esperada
    assertThrows(DuplicateResourceException.class, () -> service.create(request));

    // 3. Bonus: verifico que NUNCA haya intentado guardar
    verify(userRepository, never()).save(any(User.class));
}
```

Este patrón se llama **Arrange-Act-Assert (AAA)** y es el que seguimos en
todos los tests:

1. **Arrange**: instanciar la clase, armar el `request`/los datos de entrada,
   configurar los mocks (`when(...).thenReturn(...)`).
2. **Act**: llamar al método que estás probando.
3. **Assert**: verificar el resultado (`assertThat`, `assertThrows`) o que se
   haya llamado (o no) a algo (`verify(...)`).

---

## 6. Recetas para lo más común

**Simular que un repository encuentra algo:**
```java
when(vacancyApplicationRepository.findById("app-1")).thenReturn(Optional.of(existing));
```

**Simular que un repository NO encuentra nada:**
```java
when(vacancyApplicationRepository.findById("app-1")).thenReturn(Optional.empty());
```

**Simular que `save(...)` devuelve lo mismo que le pasaron** (lo más común,
porque en la vida real Hibernate hace eso):
```java
when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
```

**Probar que se lanza una excepción de negocio:**
```java
assertThrows(InvalidStatusTransitionException.class,
        () -> service.update("app-1", VacancyApplicationStatus.PENDIENTE));
```

**Probar un valor de retorno:**
```java
User created = service.create(request);
assertThat(created.getStatus()).isEqualTo(AccountStatus.PENDIENTE);
```

**Probar varios valores con el mismo test (parametrizado):**
```java
@ParameterizedTest
@EnumSource(value = Role.class, names = {"ALUMNO", "EMPRESA"})
void registro_deja_al_usuario_en_estado_pendiente(Role role) { ... }
```

**Probar una validación `@NotBlank`/`@Size` del DTO** (sin Spring, con el
`Validator` de Jakarta a mano):
```java
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
```

---

## 7. Cómo nombrar los tests

Nombre del método en español, snake_case, describiendo la regla que prueba —
no el método técnico:

✅ `registro_falla_si_el_email_ya_existe`
✅ `no_se_puede_retroceder_el_estado_de_una_postulacion`
🚫 `test1`, `testCreate`, `createUserTest`

Si tenés que leer el cuerpo del test para saber qué regla de negocio prueba,
el nombre está mal.

---

## 8. Cómo correrlos

Todos los tests del proyecto:
```bash
./mvnw test
```

Solo una clase:
```bash
./mvnw test -Dtest=UserServiceImplTest
```

Solo un método:
```bash
./mvnw test -Dtest=UserServiceImplTest#registro_falla_si_el_email_ya_existe
```

Vas a ver unos **warnings** de Mockito/la JVM sobre "self-attaching agent" —
son ruido normal en JDKs nuevos, no significan que el test falló. Lo que
importa es la línea final:
```
Tests run: X, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 9. Checklist antes de dar por terminado un test

- [ ] ¿El nombre del método dice QUÉ regla de negocio prueba?
- [ ] ¿Mockeaste el `Repository` y las demás dependencias, sin tocar la base
      de datos real?
- [ ] ¿No hay `@SpringBootTest`, `@DataJpaTest` ni nada que levante contexto
      de Spring?
- [ ] ¿El test falla si borrás la regla de negocio del código? (Probalo:
      comentá el `if` que tira la excepción y corré el test — se tiene que
      poner rojo. Si sigue en verde, el test no está probando nada.)
