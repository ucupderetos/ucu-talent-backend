# HOWTO — Guías para arrancar en el proyecto

 Esta carpeta tiene guías **en lenguaje simple** para
quienes recién empiezan a desarrollar APIs con Spring y Java. No hace falta saber
mucho: están pensadas para leerse de a poco y consultarse cuando te trabás.

## Orden sugerido de lectura

Si es tu primer día, seguí este orden:

1. **[Cómo levantar el proyecto (y errores comunes)](levantar-el-proyecto-y-errores-comunes.md)**
   Dejá la app andando en tu compu y aprendé a salir de los errores típicos.

2. **[El viaje de una request](viaje-de-una-request.md)**
   Entendé cómo viaja un dato por la app y qué hace cada capa (Controller,
   Service, Repository).

3. **[Glosario de anotaciones de Spring](glosario-de-anotaciones-spring.md)**
   El diccionario de las `@cositas` raras. Para consultar, no memorizar.

4. **[Por qué usamos DTOs](por-que-usamos-dtos.md)**
   Por qué no exponemos la Entity directa y usamos clases aparte para entrar y
   salir.

5. **[Cómo manejar errores sin try-catch](manejo-de-errores-sin-try-catch.md)**
   La forma ordenada de responder errores usando el manejador central.

6. **[Cómo crear un endpoint nuevo de punta a punta](crear-un-endpoint-nuevo.md)**
   La receta paso a paso para agregar tu propia funcionalidad. (Leé las
   anteriores primero.)

7. **[Cómo funcionan las migraciones de Flyway](migracion-basedatos-flyway.md)**
   Cómo se crean y cambian las tablas de la base de datos sin romper nada.

8. **[Cómo trabajar con la base de datos local (y por qué)](base-de-datos-local.md)**
   Cada uno levanta su propia base con Docker. Por qué lo hacemos así y los pasos
   para dejarla andando.

9. **[Cómo generar tu JWT_SECRET (Windows y Mac)](crear-jwt-secret.md)**
   El paso que falta para que la app arranque: generar tu propia clave y
   pegarla en tu `.env`. No hace falta que coincida con la de nadie más.

## Para cuando tengas más base

Cuando quieras profundizar en el *porqué* técnico de las decisiones del proyecto,
mirá los **ADR** (registros de decisiones) en `docs/ADR/`.

## Regla de oro para todos

Ante la duda, **preguntá antes de tocar**. Preguntar es parte de aprender, y
evita romper cosas que afectan a todo el equipo. 
