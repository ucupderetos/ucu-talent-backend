# ADR - 0009: Relaciones entre entidades como `String` plano, sin `@ManyToOne`

**Status:** accepted
**Date:** 2026-07-15

## Contexto

El modelo de datos está lleno de relaciones: una vacante pertenece a una empresa y a un área, una
postulación referencia a una vacante y a un alumno, una carrera pertenece a un área, un área puede
tener un área padre.

JPA ofrece mapear eso con relaciones gestionadas por el ORM (`@ManyToOne`, `@OneToMany`,
`@JoinColumn`): en vez de guardar el id, la entidad guarda **el objeto entero**, y Hibernate se
encarga de traerlo cuando se accede.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "company_id")
private Company company;          // en vez de: private String companyId;
```

Hay que decidir si se usan esas relaciones o si las claves foráneas se guardan como el dato que
son. Dos restricciones pesan sobre la decisión:

- **El equipo recién empieza con Spring y con ORMs.** Las relaciones gestionadas son la fuente
  clásica de problemas difíciles de diagnosticar para quien arranca: N+1, `LazyInitializationException`,
  ciclos infinitos al serializar a JSON, cascadas que borran de más.
- **Casi todo lo que la API devuelve es un DTO, no una entidad.** La aplicación rara vez necesita
  "navegar" del objeto A al B: necesita proyectar campos de varias tablas en una respuesta.

## Decision

Las claves foráneas se modelan como **el tipo de la columna, no como el objeto**: un `String` con
el id.

```java
@Column(name = "company_id", length = 12, nullable = false)
private String companyId;

@Column(name = "area_id", length = 12, nullable = false)
private String areaId;
```

**No hay una sola anotación de relación en todo el código**: ni `@ManyToOne`, ni `@OneToMany`, ni
`@JoinColumn`. La entidad es un reflejo plano de su tabla.

De ahí salen tres consecuencias que se ven en todo el proyecto:

**1. La integridad referencial la declara la base, no el ORM.** Las FK son constraints de Postgres:

```sql
ALTER TABLE vacancy ADD CONSTRAINT fk_vacancy_company
    FOREIGN KEY (company_id) REFERENCES company(company_id);
```

**2. La existencia se valida en el service, contra el dueño del dato.** Antes de insertar, el
service pregunta al service de la otra entidad —no a su repositorio:

```java
if (!companyService.existsById(request.companyId())) {
    throw new ResourceNotFoundException("Company not found.");
}
if (!areaService.existsById(request.areaId())) {
    throw new ResourceNotFoundException("Area not found.");
}
```

Así el error llega como un `404` con mensaje claro, en vez de una violación de constraint que hay
que traducir.

**3. Traer datos de varias tablas se hace con un `JOIN` explícito.** Cuando una pantalla necesita
la vacante con el nombre de su empresa y su área, la consulta lo dice:

```sql
FROM Vacancy v
JOIN Company c ON c.companyId = v.companyId
JOIN Area ar ON ar.areaId = v.areaId
```

No hay navegación implícita: lo que se lee es exactamente lo que la base ejecuta.

## Consecuencias Positivas

- **No hay N+1 accidental.** No existe la navegación implícita que lo produce: para traer datos de
  otra tabla hay que escribir el `JOIN`, y al escribirlo se ve el costo.
- **No hay `LazyInitializationException`.** Es el error más frecuente en proyectos Spring de este
  tamaño —acceder a una relación fuera de la transacción— y acá no puede ocurrir.
- **No hay ciclos al serializar.** `Vacancy → Company → List<Vacancy>` es un bucle infinito clásico
  al armar el JSON, que se suele parchear con `@JsonIgnore` o `@JsonManagedReference`. Con ids
  planos el problema no existe.
- **La entidad pesa lo que dice pesar.** Cargar una `Vacancy` trae una fila, no un grafo de objetos
  cuyo tamaño depende de qué anotaciones tenga.
- **El SQL es predecible.** Lo que se lee en el `@Query` es lo que corre. Para un equipo que está
  aprendiendo, es la diferencia entre entender el plan de ejecución y confiar en que el ORM haga lo
  correcto.
- **Se lee sin saber JPA.** `String companyId` lo entiende cualquiera; `@ManyToOne(fetch = LAZY, cascade = ...)`
  exige saber qué hace cada atributo.

## Consecuencias Negativas

- **El compilador no ayuda.** `v.getCompanyId()` es un `String`: nada impide pasar un `areaId`
  donde va un `companyId`. Con `@ManyToOne` sería un error de tipos; acá es un bug en runtime.
- **Cada consulta compuesta hay que escribirla.** No se puede pedir `vacancy.getCompany().getName()`:
  hay que agregar el `JOIN` y proyectar. Es más código, y es deliberado — ese código es visible y
  revisable.
- **La validación de existencia es responsabilidad del service, y se puede olvidar.** Si alguien
  escribe un `create` nuevo sin el `existsById`, la FK de la base lo ataja —donde existe— pero el
  error que llega al cliente es feo. Y donde no existe la FK, no lo ataja nadie.
- **Hay tablas sin la FK declarada.** `education.student_profile_id`, `work_experience.student_profile_id`
  y `degree.area_id` no tienen `FOREIGN KEY` en Postgres: solo `NOT NULL` más la validación del
  service. Ahí la integridad depende **enteramente** de la aplicación, y un `INSERT` hecho a mano
  puede dejar filas huérfanas. Es deuda concreta, no una consecuencia inevitable de la decisión.
- **No hay borrado en cascada automático entre entidades.** Cuando borrar A implica borrar B, hay
  que escribirlo. Por eso existe `AccountFacade`, que orquesta el borrado de una cuenta con todo lo
  que cuelga de ella.

## Opciones Consideradas

- **FK como `String` plano, sin relaciones del ORM** (elegido)
- **`@ManyToOne` con `fetch = LAZY`**
- **Enfoque mixto**: relaciones donde se navega seguido, ids planos en el resto

### Justificación

- **Sobre `@ManyToOne LAZY`**: es lo que recomienda la mayoría del material de JPA, y funciona bien
  cuando el equipo domina el ciclo de vida del persistence context. El costo aparece en los bordes:
  el N+1 no se ve al escribir el código sino al medir, y la `LazyInitializationException` aparece
  recién cuando alguien serializa fuera de la transacción. Para un equipo aprendiendo y con tres
  semanas, se prefirió pagar el costo visible —escribir los `JOIN`— antes que el invisible.
- **Sobre el enfoque mixto**: es el peor de los dos mundos en un equipo que recién arranca. Obliga
  a saber en cada entidad cuál de los dos estilos aplica, y esa inconsistencia se paga en cada
  revisión de código. La uniformidad vale más que la optimización caso por caso.
- **Lo que la decisión no descarta**: se sigue usando JPA/Hibernate para el mapeo, las
  transacciones y las consultas. Lo que se evita es una funcionalidad puntual —las relaciones
  gestionadas—, no el ORM.

## Pendiente

- Agregar las `FOREIGN KEY` faltantes en `education`, `work_experience` y `degree`. Hoy esas tres
  relaciones no tienen respaldo en la base, que es la única red que queda cuando la validación del
  service falla o cuando alguien toca los datos por fuera de la aplicación.

## Referencias

- `src/main/java/ucu/retojulio2026/talent/vacancy/Vacancy.java` — `companyId` y `areaId` como `String`.
- `src/main/java/ucu/retojulio2026/talent/vacancy/VacancyServiceImpl.java` — validación con `existsById` del service dueño.
- `src/main/java/ucu/retojulio2026/talent/vacancy/VacancyRepository.java` — los `JOIN` explícitos de las consultas compuestas.
- `src/main/resources/db/migration/V13__alter_vacancy_add_fields.sql` — las FK declaradas en la base.
- [ADR-0006](0006-pk-compartida-user-perfiles.md) — el caso especial donde la FK *es* la clave primaria.
- `learning/fks-string-plano-vs-manytoone.md` — nota de estudio con el detalle técnico.
