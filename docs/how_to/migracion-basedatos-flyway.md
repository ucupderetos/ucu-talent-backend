# Guía para migraciones de Flyway (guía para arrancar)

---

## 1. ¿Qué es una base de datos y qué es una tabla?

La **base de datos** es donde el programa guarda la información de forma
permanente (los usuarios, las vacantes, etc.). Podés imaginarla como un **Excel
gigante**.

Dentro de la base hay **tablas**. Cada tabla es como **una hoja de ese Excel**,
con columnas y filas:

- Las **columnas** dicen qué datos guarda (nombre, email, contraseña...).
- Las **filas** son cada dato concreto (un usuario, otro usuario, otro...).

Ejemplo de una tabla `user`:

| user_id | name  | email             |
| ------- | ----- | ----------------- |
| 1       | Ana   | ana@mail.com      |
| 2       | Luis  | luis@mail.com     |

---

## 2. El problema que resuelve Flyway

Cuando arrancás el proyecto, la base **está vacía**: no tiene ninguna tabla.
Alguien tiene que crearlas. Y acá viene el problema:

- Si cada uno crea las tablas a mano en su computadora, **todos terminan con
  tablas distintas** y el programa falla en la máquina de uno pero anda en la de
  otro. Un caos.

**Flyway soluciona esto.** Es una herramienta que **crea y actualiza las tablas
automáticamente** cuando arrancás el proyecto, siguiendo instrucciones que están
guardadas en archivos. Así **todos tenemos exactamente la misma base**, sin tener
que hacer nada a mano.

---

## 3. La idea central: las migraciones son una lista de pasos

Flyway lee unos archivos que llamamos **migraciones**. Cada migración es **un
archivo con instrucciones** que le dicen: "creá esta tabla", "agregale esta
columna a aquella tabla", etc.

Lo más importante que tenés que entender:

> **Una migración NO es "una tabla". Una migración es UN CAMBIO.**

Un cambio puede ser:

- crear una tabla nueva ✅
- agregarle una columna a una tabla que ya existe ✅
- y otros cambios más adelante

Flyway guarda estos archivos **en orden numerado**, como los capítulos de un
libro:

```
V1  → crear la tabla de usuarios
V2  → crear la tabla de vacantes
V3  → agregarle la columna "teléfono" a los usuarios
V4  → ...
```

Cuando arrancás el proyecto, Flyway **lee esos pasos en orden, del 1 al último**,
y los va ejecutando. Al terminar, tu base tiene todas las tablas listas.

---

## 4. ¿Dónde viven estos archivos?

En esta carpeta del proyecto:

```
src/main/resources/db/migration/
```

Ahí adentro vas a ver archivos con nombres así:

```
V1__create_user_table.sql
```

Vamos a leer ese nombre por partes, porque **el nombre importa mucho**:

```
V1__create_user_table.sql
│ │  └──────┬───────┘
│ │         └── descripción: qué hace (en palabras)
│ └── DOS guiones bajos __ que separan el número de la descripción
└── "V" de versión + el número de orden (1, 2, 3...)
```

Reglas del nombre:

- Empieza con **`V`** mayúscula.
- Después va el **número** (el orden en que se ejecuta).
- Después van **dos guiones bajos** `__`.
- Después una descripción con palabras separadas por un guion bajo.
- Termina en **`.sql`**.

---

## 5. LA REGLA (¡importante!)

> **Un archivo de migración que YA existe NO se toca NUNCA.**

¿Por qué? Porque ese archivo ya se ejecutó en la base de datos de todos tus
compañeros (y quizás en la que está en internet). Flyway lleva un registro de lo
que ya hizo. Si vos editás un archivo viejo, Flyway se da cuenta de que
"cambiaste el pasado", **y el proyecto no arranca** para nadie. 

**Entonces, ¿cómo hago un cambio?**

Siempre creás un **archivo NUEVO** con el **número siguiente**.

### Ejemplo

Ya existe `V1__create_user_table.sql` (creó la tabla de usuarios).

Mañana querés **agregarle un teléfono** a los usuarios. NO abrís el V1. Creás uno
nuevo:

```
V2__add_phone_to_user.sql
```

Y adentro ponés la instrucción para agregar la columna. Al arrancar, Flyway ve
que hay un V2 nuevo y solo ejecuta ese. La tabla de usuarios queda con el
teléfono agregado. 


---

## 6. ¿Cómo sé cuál es el próximo número?

Fácil: mirá la carpeta `src/main/resources/db/migration/`, buscá el número más
alto que ya exista, y usá el que sigue.

- Si el último es `V3`, el tuyo es `V4`.
- Si el último es `V10`, el tuyo es `V11`.

**Nunca repitas un número** (no puede haber dos `V2`) y **nunca uses uno del
medio ya existente**.

---

## 7. Pasos para crear tu migración (checklist)

Cuando necesites crear o cambiar una tabla, seguí estos pasos:

1. Andá a la carpeta `src/main/resources/db/migration/`.
2. Fijate cuál es el número más alto que ya existe.
3. Creá un archivo nuevo con el número siguiente y una descripción clara, por
   ejemplo: `V4__create_vacancy_table.sql`.
4. Escribí adentro la instrucción del cambio.
5. **Guardá y arrancá el proyecto.** Flyway aplica tu cambio automáticamente.
6. Fijate en la consola que no aparezcan errores.

---

## 8. Un detalle que te va a pasar seguido

En este proyecto, además de la migración, muchas veces hay que tocar una **clase
de Java** que "representa" la tabla (las llamamos *entidades*, tienen `@Entity`
arriba). El programa tiene un chequeo que, al arrancar, compara la tabla real con
esa clase de Java. Si no coinciden, **te avisa y no arranca** (es a propósito,
para proteger).

O sea: si agregás una columna en la migración, probablemente tengas que agregar
también ese dato en la clase de Java. Si el proyecto no arranca y el error habla
de una columna que "falta" o "sobra", casi seguro es esto. Pedí ayuda y se
resuelve rápido.

