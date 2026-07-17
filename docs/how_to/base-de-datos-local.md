# Cómo trabajar con la base de datos local (y por qué)

> **Resumen:** cada uno trabaja contra una base de datos **propia**, que corre en
> Docker en tu computadora. Ya no apuntamos a la base que está en Google Cloud.
> Es una medida **temporal** y **de orden**, para que el versionado de la base no
> se rompa. Los pasos concretos están en la sección 5.

---

## 1. Cómo era antes y qué cambió

Hasta ahora, todos apuntábamos a **una sola base de datos hosteada** en Google
Cloud. Una sola base, compartida por todo el equipo.

A partir de ahora, **cada uno levanta su propia base con Docker**. Es tuya, corre
en tu máquina, y no la ve nadie más.

Antes de ver los pasos, conviene entender el problema que esto resuelve. 

---

## 2. El problema: una sola base compartida y muchas ramas

Acordate de lo que dice la [guía de Flyway](migracion-basedatos-flyway.md): las
migraciones son **una lista de pasos numerados** (V1, V2, V3...) que se ejecutan
**en orden**.

Lo que quizás no sabías: **Flyway anota lo que ya hizo dentro de la propia base
de datos**, en una tabla que se llama `flyway_schema_history`. Esa tabla es su
memoria. Ahí guarda "ya apliqué hasta la V8".

Y acá está el quilombo:

> **Git y la base de datos son cosas separadas.** Git guarda tus archivos por
> rama. La base de datos **no tiene ramas**: es una sola, y no le importa en qué
> rama estás parado.

Entonces, si todos apuntamos a la misma base compartida:

- Vos estás en tu rama `feature/mi-cosa`, creás una `V9` y arrancás el proyecto.
- Flyway aplica tu `V9` **a la base compartida**, que es la de todos.
- Ahora la base tiene un cambio de una rama que **todavía no está en `dev`**.

La base quedó en un estado que **no corresponde a ninguna rama de verdad**. Es un
Frankenstein: tiene pedazos de la rama de uno y pedazos de la de otro.

### Cómo se rompe en la práctica

Estos tres casos pasan solos, sin que nadie haga nada mal a propósito:

**a) Dos `V9` distintas.** Vos hacés `V9__agregar_columna_x.sql` en tu rama. Un
compañero, el mismo día, hace `V9__crear_tabla_y.sql` en la suya. El que arranca
primero le mete su V9 a la base compartida. Cuando el segundo arranca, Flyway
mira su memoria, ve que "la V9 ya está aplicada"... pero era **otra** V9. Se
rompe todo.

**b) La base va más adelante que el código.** Le aplicaste la V9 a la base
compartida desde tu rama. Un compañero, que está en `dev` (donde la V9 todavía no
existe), arranca el proyecto y le falla: su código no sabe nada de esa columna
nueva, pero la base ya la tiene.

**c) No hay vuelta atrás.** Si aplicaste una migración con un error, **no se
puede deshacer**. Flyway no tiene botón de "deshacer". La columna quedó ahí, con
el error, para todo el equipo. Y como es la base compartida, tampoco podés
borrarla y empezar de nuevo.

---

## 3. La solución: tu propia base, descartable

Con una base local en Docker, los tres problemas de arriba **desaparecen**:

- **Tu base sigue a tu rama.** Cambiás de rama, arrancás, y Flyway aplica lo que
  esa rama tenga. No hay Frankenstein.
- **Podés romper todo tranquilo.** ¿Quedó la base en un estado raro? La borrás
  con un comando y Flyway la reconstruye desde la V1. Nadie se entera.
- **Nadie te rompe a vos.** Lo que hace un compañero en su rama no te toca.

### ¿Y la base compartida?

Sigue existiendo (es la de `dev`), pero **le entran migraciones por un solo
camino: cuando el cambio ya está mergeado en `dev`**.

Esto **no es por capricho**. Es simplemente **orden**: si las
migraciones entran de a una y en el mismo orden en que se mergean a `dev`, la
numeración (V9, V10, V11...) queda igual en la base que en el código, y el
versionado no se rompe. Si entraran desde cinco ramas a la vez, el orden de la
base sería el orden en que cada uno apretó "run" — que no es el orden de nada.

Es la misma lógica de por qué se mergea a `dev` con Pull Request y no empujando
directo: un solo canal, un solo orden, y así se puede saber en qué estado está.

---

## 4. Por qué es temporal

Esto es un **arreglo de ahora**, no la solución definitiva. Lo correcto, más
adelante, es que las migraciones a la base compartida las aplique el pipeline
automáticamente al mergear a `dev`, y/o tener una base separada por ambiente. Eso
es tema de infraestructura y no está hecho todavía.

Mientras tanto, base local para todos. Cuando exista lo otro, esta guía se cae.

---

## 5. Pasos para trabajar local (checklist)

Se hace **una sola vez**:

**1. Copiá el archivo de configuración de ejemplo:**

```bash
cp .env.example .env
```

Ese `.env.example` ya viene configurado en modo local. No necesitás cambiarle
nada, ni pedirle credenciales a nadie.

**2. Levantá todo:**

```bash
docker compose up --build
```

Eso arranca dos cosas: la base de datos Postgres y la app. Flyway corre solo y
crea todas las tablas desde la V1.

> Usá **siempre** `--build`. La razón está explicada en la sección 6, pero la
> regla corta es: sin `--build` podés terminar corriendo código viejo sin
> enterarte.

**3. Verificá que arrancó.** En la consola tenés que ver algo así:

```
Successfully applied 9 migrations to schema "public", now at version v9
...
Started TalentApplication in 14.188 seconds
```

Listo. La app queda en http://localhost:8080 y la documentación en
http://localhost:8080/docs

---

## 6. El día a día

### Cuando alguien mergea a `dev` (o cada vez que hacés `git pull`)

```bash
git pull
docker compose up --build
```

> **El `--build` no es opcional.** Es el error más fácil de cometer y el más
> difícil de darte cuenta. Ver la explicación abajo.

**No hace falta borrar nada.** Flyway mira su memoria, ve que le falta la V10, y
aplica **solo esa**. Tus datos de prueba quedan donde estaban.

#### Por qué el `--build`

Cuando corrés la app con Docker, tu código **no** se lee de tu carpeta: se
"cocina" adentro de una **imagen** (un paquete con el código ya compilado). Las
migraciones también viajan adentro de esa imagen.

El problema es que `docker compose up`, **sin `--build`, reusa la imagen que ya
tenías**. No se entera de que hiciste `git pull`. Entonces:

- Bajás la V10 que hizo un compañero.
- Corrés `docker compose up`.
- La app arranca **con el código de ayer**, y Flyway ni ve la V10.
- **No te da ningún error.** Arranca todo bien. Vos creés que estás actualizado,
  y en realidad estás corriendo lo viejo.

Con `--build` la imagen se re-cocina con lo que acabás de bajar, y ahí sí Flyway
ve la V10 y la aplica.

Si dudás, ponéle `--build` igual: cuando no hay nada nuevo tarda unos segundos y
no cambia nada.

### Cuando querés empezar de cero

Si tu base quedó en un estado raro, o querés probar que las migraciones funcionan
desde el principio:

```bash
docker compose down -v
docker compose up --build
```

Ese `-v` es el que **borra la base**. Flyway la reconstruye desde la V1. Perdés
los datos de prueba que hayas cargado, pero no perdés nada importante: los datos
posta no están acá.

### Cuando creás una migración nueva

Igual que siempre (mirá la [guía de Flyway](migracion-basedatos-flyway.md)), con
una ventaja: ahora la probás **en tu base**, sin miedo. Si te equivocaste,
`docker compose down -v` y de nuevo.

---

## 7. Errores comunes

**"port is already allocated" / el puerto 5432 está ocupado**

Ya tenés algo usando ese puerto (otro Postgres, o un contenedor viejo dando
vueltas). Dos opciones:

```bash
docker compose down --remove-orphans   # limpia contenedores viejos del proyecto
```

O cambiá el puerto en tu `.env`:

```bash
POSTGRES_PORT=55432
```

**"Migration checksum mismatch"**

Alguien editó una migración que vos ya tenías aplicada (o la editaste vos). Como
tu base es descartable, se arregla así:

```bash
docker compose down -v
docker compose up --build
```

**El proyecto no arranca y habla de una columna que "falta" o "sobra"**

Es el chequeo que compara la tabla real contra la clase de Java (`@Entity`). Está
explicado en la [sección 8 de la guía de Flyway](migracion-basedatos-flyway.md).

---

## 8. La regla corta

> **Si tenés que pedirle credenciales a alguien para levantar el proyecto, algo
> está mal.** Con `cp .env.example .env` y `docker compose up --build` tiene que
> alcanzar.

Y la otra, que es la que más dolores de cabeza ahorra:

> **Después de cada `git pull`, `docker compose up --build`.** Sin el `--build`
> corrés lo viejo y no te avisa nadie.
