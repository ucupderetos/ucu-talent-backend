# Diagramas de flujo — UCU Talent

Diagramas para presentar el funcionamiento de la plataforma. Están escritos en lenguaje de
producto, sin endpoints ni detalles técnicos: la equivalencia con la API está al final de cada
sección, para consulta interna.

---

## 1. El camino del alumno

Desde que se registra hasta que sabe si quedó seleccionado.

```mermaid
flowchart TD
    A([El alumno se registra]) --> B[Completa su perfil:<br/>datos personales, skills, CV]
    B --> C[Carga su formación:<br/>carrera e institución]
    C --> D{El Admin UCU<br/>revisa la cuenta}
    D -->|Rechazada| E([No puede postularse])
    D -->|Aprobada| F[Busca puestos<br/>por área, modalidad y localidad]
    F --> G[Se postula a un puesto]
    G --> H[/La empresa recibe un mail:<br/>tenés una postulación nueva/]
    G --> I[Su postulación queda PENDIENTE]
    I --> J{La empresa la revisa}
    J -->|La marca como vista| K[/El alumno recibe un mail:<br/>vieron tu postulación/]
    J -->|La marca como aceptada| L[Sigue en el proceso<br/>de selección]
    K --> M{Se cierra la búsqueda}
    L --> M
    M -->|Estaba aceptado| N[/Mail: fuiste seleccionado/]
    M -->|No estaba aceptado| O[/Mail: la búsqueda se cerró/]
    N --> P([El alumno ve el resultado<br/>en Mis Postulaciones])
    O --> P

    style A fill:#e8f0fe,stroke:#4285f4
    style P fill:#e6f4ea,stroke:#34a853
    style E fill:#fce8e6,stroke:#ea4335
    style D fill:#fef7e0,stroke:#fbbc04
```



<details>
<summary>Equivalencia con la API (interno)</summary>

| Paso | Endpoint |
|---|---|
| Se registra | `POST /user` (nace `PENDIENTE`) |
| Completa perfil | `POST /student-profile` · `PATCH /student-profile/cv` |
| Carga formación | `POST /education` |
| El Admin lo aprueba | `PATCH /user/{id}` → `APROBADO` |
| Busca puestos | `GET /vacancy/student/search` |
| Se postula | `POST /vacancy-application` (nace `PENDIENTE`) |
| Ve sus postulaciones | `GET /vacancy-application/me/detailed` |

</details>

---

## 2. El camino de la empresa

En espejo con el anterior: desde que se registra hasta que cierra la búsqueda.

```mermaid
flowchart TD
    A([La empresa se registra]) --> B[Completa sus datos:<br/>razón social, rubro, ubicación]
    B --> C{El Admin UCU<br/>revisa la cuenta}
    C -->|Rechazada| D([No puede publicar])
    C -->|Aprobada| E[Publica un puesto]
    E --> F[El puesto queda visible<br/>al instante para los alumnos]
    F --> G[/Recibe un mail por<br/>cada postulación nueva/]
    G --> H[Ve su tablero:<br/>puestos, total de postulantes<br/>y cuántos sin revisar]
    H --> I[Abre un puesto y ve<br/>los candidatos con su CV]
    I --> J[Marca postulaciones<br/>como vistas]
    J --> K[Acepta a los candidatos<br/>que siguen en el proceso]
    K --> L{Cierra la búsqueda}
    L --> M[Se cierran todas<br/>sus postulaciones]
    M --> N([Cada postulante recibe<br/>el mail que le corresponde])

    style A fill:#e8f0fe,stroke:#4285f4
    style N fill:#e6f4ea,stroke:#34a853
    style D fill:#fce8e6,stroke:#ea4335
    style C fill:#fef7e0,stroke:#fbbc04
    style F fill:#e6f4ea,stroke:#34a853
```

**El puesto se publica y se ve al instante** — la revisión del Admin viene después. Es una decisión
de diseño de la plataforma, no un descuido: se llama *moderación post-publicación*.

<details>
<summary>Equivalencia con la API (interno)</summary>

| Paso | Endpoint |
|---|---|
| Se registra | `POST /user` (nace `PENDIENTE`) |
| Completa datos | `POST /company` |
| El Admin la aprueba | `PATCH /user/{id}` → `APROBADO` |
| Publica un puesto | `POST /vacancy` (nace `PUBLICADO`) |
| Su tablero | `GET /vacancy/company/{companyId}/management` |
| Candidatos de un puesto | `GET /vacancy-application/vacancy/{vacancyId}/detailed` |
| Marca como vista | `PUT /vacancy-application/{id}` → `VISTO` |
| Acepta un candidato | `PATCH /vacancy-application/{id}/accept` |
| Cierra la búsqueda | `PATCH /vacancy/status/{id}` → `FINALIZADO` |

</details>

---

## 3. Ciclo de vida de un puesto


```mermaid
flowchart LR
    N([La empresa<br/>publica el puesto]) --> P

    P[PUBLICADO<br/><i>visible para los alumnos</i>]
    R[EN REVISIÓN<br/><i>el Admin lo bajó</i>]
    F[FINALIZADO<br/><i>estado final</i>]

    P -->|El Admin lo pone en revisión| R
    R -->|El Admin lo republica| P

    P -->|1. La empresa cierra la búsqueda| F
    P -->|2. El Admin la cierra| F
    P -->|3. Se vence la fecha de cierre| F
    P -->|4. La empresa da de baja el puesto| F

    F --> C[Se cierran todas<br/>sus postulaciones]
    C --> M[/Cada postulante<br/>recibe un mail/]

    style P fill:#e6f4ea,stroke:#34a853
    style R fill:#fef7e0,stroke:#fbbc04
    style F fill:#f1f3f4,stroke:#5f6368
    style N fill:#e8f0fe,stroke:#4285f4
```


<details>
<summary>Equivalencia con la API (interno)</summary>

| Transición | Disparador |
|---|---|
| Nace `PUBLICADO` | `POST /vacancy` (`Vacancy.assignId()` lo setea si viene null) |
| `PUBLICADO` → `PENDIENTE` / vuelta | `PUT /vacancy/status/{id}` (solo ADMIN) |
| 1. Cierre de la empresa | `PATCH /vacancy/status/{id}` → `FINALIZADO` |
| 2. Cierre del Admin | `PUT /vacancy/status/{id}` → `FINALIZADO` |
| 3. Vencimiento | `VacancyServiceImpl.finalizeExpiredVacancies()`, cron diario 00:00 |
| 4. Baja lógica | `DELETE /vacancy/{id}` (`deleted = true`, no borra la fila) |
| Efecto común | `finalizeByVacancyId(...)` + `VacancyFinalizationNotifier` |

</details>

---

## 4. Ciclo de vida de una postulación

Tres estados que **solo avanzan**. Una postulación nunca vuelve a un estado anterior.

```mermaid
flowchart LR
    A([El alumno se postula]) --> P
    P[PENDIENTE<br/><i>la empresa no la miró todavía</i>]
    V[VISTO<br/><i>la empresa la revisó</i>]
    F[FINALIZADO<br/><i>estado final</i>]

    P -->|La empresa la abre| V
    P -->|Se cierra la búsqueda| F
    V -->|Se cierra la búsqueda| F
    V -.->|nunca retrocede| P

    F --> Q{¿Estaba marcada<br/>como aceptada?}
    Q -->|Sí| S[/Mail: fuiste seleccionado/]
    Q -->|No| NS[/Mail: la búsqueda se cerró/]

    style P fill:#fef7e0,stroke:#fbbc04
    style V fill:#e8f0fe,stroke:#4285f4
    style F fill:#f1f3f4,stroke:#5f6368
    style A fill:#e8f0fe,stroke:#4285f4
    style Q fill:#fef7e0,stroke:#fbbc04
    style S fill:#e6f4ea,stroke:#34a853
    linkStyle 4 stroke:#ea4335,color:#ea4335
```

La marca de **aceptada** corre por un carril aparte: no cambia el estado, se puede poner en
cualquier momento antes del cierre, y es lo único que decide cuál de los dos correos recibe el
alumno.

```mermaid
flowchart LR
    V[Postulación en VISTO] -->|La empresa la acepta| AC[Aceptada = sí<br/><i>sigue en el proceso</i>]
    V -->|La empresa no la acepta| NO[Aceptada = no]
    AC -.->|al cerrarse la búsqueda| S[/Mail: fuiste seleccionado/]
    NO -.->|al cerrarse la búsqueda| N[/Mail: la búsqueda se cerró/]

    style AC fill:#e6f4ea,stroke:#34a853
    style NO fill:#f1f3f4,stroke:#5f6368
    style S fill:#e6f4ea,stroke:#34a853
```

Y el cierre no lo hace la empresa postulación por postulación: cuando el puesto se cierra —por
cualquiera de sus cuatro caminos— **todas sus postulaciones pasan a `FINALIZADO` de una vez**.

```mermaid
flowchart LR
    X([El puesto se cierra]) --> Y[Todas sus postulaciones<br/>pasan a FINALIZADO]
    Y --> Z[/Cada postulante recibe<br/>el mail que le corresponde/]

    style X fill:#f1f3f4,stroke:#5f6368
    style Y fill:#f1f3f4,stroke:#5f6368
```

<details>
<summary>Equivalencia con la API (interno)</summary>

| Transición | Disparador |
|---|---|
| Nace `PENDIENTE` | `POST /vacancy-application` |
| `PENDIENTE` → `VISTO` | `PUT /vacancy-application/{id}` (empresa dueña de la vacante) |
| Marca de aceptada | `PATCH /vacancy-application/{id}/accept` (flag `accepted`, no es un estado) |
| Cierre en cascada | `finalizeByVacancyId(...)`, disparado por los 4 caminos del diagrama 3 |
| Retroceso bloqueado | `InvalidStatusTransitionException` → `409`, comparando `status.ordinal()` |
| El alumno la retira | `DELETE /vacancy-application/{id}` (solo el postulante) |

</details>

---

## 5. Moderación post-publicación

Cómo se modera el contenido, comparado con lo que hace la mayoría de los portales.

**Moderación previa — lo que hace la mayoría de los portales**

```mermaid
flowchart LR
    O1[La empresa<br/>publica un puesto] --> O2[Queda en espera<br/><i>nadie lo ve</i>]
    O2 --> O3{El Admin<br/>lo revisa}
    O3 -->|Lo aprueba| O4[Recién ahí<br/>lo ven los alumnos]
    O3 -->|Lo rechaza| O5[No se publica nunca]

    style O2 fill:#fce8e6,stroke:#ea4335
    style O3 fill:#fef7e0,stroke:#fbbc04
    style O5 fill:#fce8e6,stroke:#ea4335
```

**Moderación post-publicación — UCU Talent**

```mermaid
flowchart LR
    N1[La empresa<br/>publica un puesto] --> N2[Los alumnos<br/>lo ven al instante]
    N2 --> N3{El Admin<br/>lo revisa después}
    N3 -->|Está bien| N4[Sigue publicado]
    N3 -->|Está mal| N5[Lo baja o lo cierra]

    style N2 fill:#e6f4ea,stroke:#34a853
    style N3 fill:#fef7e0,stroke:#fbbc04
    style N4 fill:#e6f4ea,stroke:#34a853
    style N5 fill:#fef7e0,stroke:#fbbc04
```



<details>
<summary>Equivalencia con la API (interno)</summary>

| Paso | Detalle |
|---|---|
| Nace publicado | `Vacancy` se persiste con `status = PUBLICADO` si no viene otro |
| El Admin lo baja | `PUT /vacancy/status/{id}` → `PENDIENTE` ("El Puesto está en revisión") |
| El Admin lo cierra | `PUT /vacancy/status/{id}` → `FINALIZADO` + comentario del Admin |
| Mientras está en revisión | la empresa no puede cambiarle el estado (`403`) |

</details>

---

## 6. Alcance de cada rol

Cada rol opera únicamente sobre lo suyo. La plataforma verifica en cada pedido quién lo está
haciendo.

```mermaid
flowchart LR
    AL([👤 Alumno]) --> A1[Completa su perfil,<br/>su formación y su CV]
    AL --> A2[Busca puestos publicados]
    AL --> A3[Se postula]
    AL --> A4[Ve solo SUS postulaciones]
    AL --> A5[Retira una postulación suya]

    style AL fill:#e8f0fe,stroke:#4285f4
```

```mermaid
flowchart LR
    EM([🏢 Empresa]) --> E1[Completa los datos<br/>de la empresa]
    EM --> E2[Publica y edita<br/>SUS puestos]
    EM --> E3[Ve los postulantes de SUS<br/>puestos, con su CV]
    EM --> E4[Marca vistos y<br/>acepta candidatos]
    EM --> E5[Cierra o da de baja<br/>SUS búsquedas]

    style EM fill:#e6f4ea,stroke:#34a853
```

```mermaid
flowchart LR
    AD([🛡️ Admin UCU]) --> D1[Aprueba o rechaza<br/>alumnos y empresas]
    AD --> D2[Modera cualquier puesto]
    AD --> D3[Ve todas las postulaciones<br/>del sistema]
    AD --> D4[Ve el panel con los<br/>totales de la plataforma]
    AD --> D5[Consulta la auditoría de<br/>acciones administrativas]

    style AD fill:#fef7e0,stroke:#fbbc04
```


<details>
<summary>Equivalencia con la API (interno)</summary>

| Mecanismo | Detalle |
|---|---|
| Identidad | JWT en cookie `HttpOnly`; el id sale del token, nunca del body |
| Por rol | `hasRole("ALUMNO" \| "EMPRESA" \| "ADMIN")` en `SecurityConfig` |
| Por dueño | `AuthorizationGuard.requireOwnership(...)` / `requireOwnershipOrRoles(..., "ADMIN")` |
| Ejemplo de dueño | `GET /vacancy-application/vacancy/{vacancyId}/detailed` compara el `companyId` de la vacante contra el del token |
| Ejemplo sin id | `GET /vacancy-application/me/detailed` no recibe id: sale del token, así no hay forma de pedir el de otro |

</details>
