# ucu-talent-backend

API Rest del proyecto **Talent** (Reto Julio 2026 - UCU).

## Stack

- **Java 21**
- **Spring Boot 4.0.7** (Web MVC, Data JPA, Security, OAuth2 Resource Server, Validation)
- **PostgreSQL** como base de datos
- **Flyway** para migraciones de base de datos
- **springdoc-openapi** (Swagger UI) para documentación de la API
- **Lombok**
- **Maven** (con wrapper `mvnw`)

## Arquitectura del proyecto

Se utiliza **arquitectura en capas** (*layered architecture*). Se eligió por dos motivos:

- **Simplicidad y velocidad de desarrollo:** es un patrón conocido y con soporte directo en Spring Boot, lo que reduce la curva de aprendizaje del equipo y acelera la entrega, algo clave en el contexto acotado del reto.
- **Separación de responsabilidades:** cada capa tiene una única responsabilidad y se comunica solo con la capa adyacente, lo que facilita el mantenimiento, las pruebas y la evolución del código.

### Capas

Las capas se organizan de arriba (más cercana al cliente) hacia abajo (más cercana a los datos). Cada capa depende únicamente de la que tiene debajo:

#### Controller
Es la puerta de entrada de la aplicación. Expone los endpoints REST, valida la entrada, delega la lógica en la capa de *Service* y traduce el resultado a una respuesta HTTP (DTO/JSON). No contiene lógica de negocio.

#### Service
Contiene la **lógica de negocio** de la aplicación. Orquesta las operaciones, aplica las reglas del dominio, gestiona las transacciones y coordina uno o más *Repository*. Es independiente del protocolo HTTP.

#### Repository
Es la capa de **acceso a datos**. Abstrae la persistencia mediante Spring Data JPA, encapsulando las consultas a PostgreSQL para que las capas superiores no dependan de detalles de la base de datos.

#### Model
Representa el **dominio/modelo**: mapea las tablas de la base de datos a entidades JPA (clases Java). Es la estructura de datos que atraviesa todas las capas.



### Modularización

El software se modulariza **por dominio/modelo** y no por capa técnica. Es decir, cada agrupación (`User`, `Area`, etc.) contiene sus propias capas, en lugar de tener carpetas globales `controllers/`, `services/`, etc.

Esto mantiene junto todo lo relacionado con un mismo concepto de negocio (alta cohesión, bajo acoplamiento entre módulos) y facilita una eventual migración hacia **microservicios**, donde cada módulo podría extraerse como un servicio independiente.

Ejemplo:

```
User
├── Model
├── Service
├── Controller
└── Repository

Area
├── Model
├── Service
├── Controller
└── Repository
```

### Flujo de datos

Una request entra por el *Controller* y desciende por las capas hasta la base de datos; la respuesta recorre el camino inverso, transformándose en un DTO antes de salir:

```
                 (entrada)                              (salida)
Request → Controller → Service → Repository → DB → Repository → Service → Controller → DTO/JSON
              │                                                                  │
              └──────────────── valida y mapea a DTO/entidad ───────────────────┘
```

1. **Controller** recibe la `Request`, valida los datos y los mapea al modelo/DTO.
2. **Service** aplica la lógica de negocio y solicita los datos al *Repository*.
3. **Repository** consulta o persiste en PostgreSQL y devuelve entidades.
4. El resultado vuelve por *Service* → *Controller*, que lo serializa como **DTO/JSON** de salida.

## Requisitos previos

- JDK 21
- PostgreSQL en ejecución

## Configuración

Conexión a la base de datos en `src/main/resources/application.properties`
(o mediante variables de entorno). Por ejemplo:

```properties
spring.application.name=talent
spring.datasource.url=jdbc:postgresql://...
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## Cómo ejecutar

```bash
# Compilar
./mvnw clean compile

# Ejecutar la aplicación
./mvnw spring-boot:run

# Ejecutar los tests
./mvnw test

# Generar el .jar
./mvnw clean package
```

> En Windows `mvnw.cmd` en lugar de `./mvnw`.

Una vez levantado, la documentación de la API queda disponible en:

```
http://localhost:8080/swagger-ui.html
```

## Estructura del proyecto

```
ucu-talent-backend/
├── src/
│   ├── main/
│   │   ├── java/ucu/retojulio2026/talent/   # código fuente
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/                # migraciones Flyway
│   └── test/                                # tests
├── pom.xml
└── mvnw / mvnw.cmd
```
