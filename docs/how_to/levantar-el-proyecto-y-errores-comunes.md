# Cómo levantar el proyecto (y qué hacer cuando algo falla)

Esta guía es para tu **día 1**: dejar el proyecto andando en tu computadora, y
saber qué hacer con los errores más típicos que le pasan a todo el mundo al
empezar. No te asustes si algo falla la primera vez: es normal, y casi siempre es
una de las cosas que están más abajo.

---

## 1. Qué necesitás instalado

- **Java 21** (el lenguaje).
- **Docker** (para levantar la base de datos sin instalar Postgres a mano).
- **Git** (para bajar el proyecto).

No hace falta instalar Maven: el proyecto trae su propio "Maven de bolsillo"
llamado `./mvnw`.

---

## 2. Preparar el archivo de configuración (`.env`)

El proyecto necesita un archivo `.env` con datos como el usuario y la contraseña
de la base. **No viene incluido** (tiene datos que no se suben a git), pero hay
una plantilla. Copiala:

```bash
cp .env.example .env
```

Con eso alcanza para desarrollo local. Si abrís el `.env` vas a ver algo así:

```
POSTGRES_DB=talent
POSTGRES_USER=talent_user
POSTGRES_PASSWORD=password
POSTGRES_PORT=5432
APP_PORT=8080
```

---

## 3. Levantar todo con Docker (la forma más fácil)

Un solo comando levanta **la base de datos y la API** juntas:

```bash
docker compose up
```

La primera vez tarda un poco (baja e instala cosas). Cuando veas en la consola
algo como `Started TalentApplication`, ya está andando. 🎉

Para apagarlo: apretá `Ctrl + C`, o desde otra terminal:

```bash
docker compose down
```


## 4. Comprobar que anda: Swagger 🔍

Abrí en el navegador:

**http://localhost:8080/docs**

Swagger es una página donde ves **todos los endpoints** de la API y podés
probarlos sin escribir código, con el botón "Try it out". Si la ves, ¡todo
funciona!

---

## 5. Errores comunes y cómo resolverlos 

Acá están los que le pasan a todo el mundo. Buscá tu error en la lista.

### ❌ "Port 8080 is already in use" (o el 5432)

**Qué significa:** ya hay otro programa usando ese puerto (quizás una copia
vieja del proyecto que quedó prendida).

**Solución:**
- Cerrá la otra instancia (`docker compose down`, o cerrá la terminal vieja).
- O cambiá el puerto en tu `.env` (por ejemplo `APP_PORT=8081`) y entrá a
  `http://localhost:8081/docs`.

---

### ❌ La app no arranca y habla de "connection refused" a la base de datos

**Qué significa:** la API arrancó pero no encuentra la base de datos.

**Solución:**
- Asegurate de que la base esté levantada (`docker compose up db`).
- Si usás Docker para todo (`docker compose up`), esto no debería pasar porque la
  API espera a que la base esté lista.
- Revisá que el `.env` exista y tenga los datos correctos.

---

### ❌ "Migration checksum mismatch" (error de Flyway)

**Qué significa:** alguien **editó una migración que ya se había aplicado**. Flyway
se da cuenta de que "cambió el pasado" y frena.

**Solución:**
- La regla es **nunca editar migraciones viejas** (ver
  `HOWTO/migracion-basedatos-flyway.md`). Si fuiste vos, revertí el cambio.


---

### ❌ "Schema validation: ... column ... but expecting ..." (error de Hibernate)

**Qué significa:** una tabla y su clase Java (`@Entity`) **no coinciden**. Por
ejemplo, agregaste un campo en la Entity pero te olvidaste de la migración (o al
revés).

**Solución:**
- Revisá que los campos de tu Entity coincidan con las columnas de la migración
  (nombres y tipos).
- Si te falta un cambio en la base, creá una migración nueva con ese cambio.
- Este error es **a propósito**: te avisa temprano en vez de fallar después.

---

### ❌ El proyecto no compila y habla de "cannot find symbol" con getters/setters o el Mapper

**Qué significa:** faltó generar el código automático de Lombok o MapStruct.

**Solución:**
- Compilá de nuevo limpiando primero:
  ```bash
  ./mvnw clean compile
  ```
- Si usás un editor (IntelliJ / VS Code), asegurate de tener el plugin de Lombok
  instalado y la "annotation processing" activada.

---

### ❌ Cambié código pero no veo el cambio

**Solución:**
- Si corriste con `./mvnw spring-boot:run`, cortá con `Ctrl + C` y volvé a
  arrancar.
- Si usás Docker y cambiaste código, reconstruí la imagen:
  ```bash
  docker compose up --build
  ```

---

## 6. Comandos que vas a usar todo el tiempo 📋

```bash
cp .env.example .env          # (solo la primera vez) crear tu configuración

docker compose up             # levantar base + API
docker compose up --build     # idem, reconstruyendo la API tras cambios de código
docker compose up db          # levantar solo la base de datos
docker compose down           # apagar todo (los datos se conservan)


./mvnw spring-boot:run        # correr la API en tu máquina (sin Docker)
./mvnw clean compile          # compilar de cero
./mvnw test                   # correr los tests
```

---

## 7. Si nada de esto funciona 🆘

1. **Leé el error completo** en la consola: casi siempre la última línea (o la
   que dice `Caused by:`) te dice qué pasó.
2. **Copiá el error y buscalo** en la lista de arriba.

