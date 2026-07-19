# Cómo generar tu JWT_SECRET (Windows y Mac)

> **Resumen:** el `.env.example` te pide un `JWT_SECRET` random. Cada uno genera
> el suyo, en su propia compu, y lo pega en su propio `.env`. **No hace falta que
> coincida con el de nadie más** (ver sección 2). Los comandos están en la
> sección 3.

---

## 1. Qué es y para qué sirve

La app firma los tokens de login (JWT) con una clave secreta (`JWT_SECRET`),
usando el algoritmo HMAC256. Esa clave es la que después usa para **validar**
que un token no fue falsificado.

Sin un `JWT_SECRET` en tu `.env`, la app ni arranca (ver
[`levantar-el-proyecto-y-errores-comunes.md`](levantar-el-proyecto-y-errores-comunes.md)
si te tropezás con otros errores al arrancar).

## 2. ¿Todos necesitamos el mismo secreto?

**No, mientras trabajemos en modo `local`.** Cada uno levanta su propia base
Postgres en Docker (ver
[`base-de-datos-local.md`](base-de-datos-local.md)) y su propia instancia de la
app. Vos te logueás contra tu propia app, que firma tu token y después lo valida
ella misma. Nunca hace falta que otra persona valide un token que generaste vos.

Por eso: **generá el tuyo, propio y random, y no se lo pidas a nadie ni lo
compartas.** El día que exista un ambiente `hosted` compartido por el equipo,
esa parte se documenta aparte (por ahora no está levantado).

## 3. Generar el secreto

Necesitás una cadena random de al menos 32 caracteres. Elegí tu sistema:

### Mac / Linux (o Windows con Git Bash / WSL)

En una terminal:

```bash
openssl rand -base64 48
```

Te va a tirar algo como:

```
K3f9s0m2Qw7VxYd1... (una tira larga de letras, números y algún +, / o =)
```

### Windows (PowerShell, sin Git Bash ni WSL)

Windows no trae `openssl` instalado por defecto. Usá PowerShell con esto en su
lugar, que genera la misma clase de secreto usando el generador random
criptográfico de .NET:

```powershell
$bytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

Pegá las tres líneas juntas en PowerShell y apretá Enter. Te devuelve una tira
larga, igual que con `openssl`.

> Si tenés Git Bash o WSL instalado, también podés usar el comando de
> Mac/Linux de arriba — da lo mismo, es el mismo algoritmo.

## 4. Copiarlo a tu `.env`

**1.** Si todavía no tenés tu `.env` local, creálo primero (ver
[`base-de-datos-local.md`](base-de-datos-local.md)):

```bash
cp .env.example .env
```

**2.** Abrí `.env` con tu editor y buscá esta línea:

```
JWT_SECRET=cambiar-por-un-secreto-random-de-32-caracteres-o-mas
```

**3.** Reemplazá el valor por lo que generaste en el paso 3, sin comillas ni
espacios:

```
JWT_SECRET=K3f9s0m2Qw7VxYd1...(tu-tira-completa-aca)
```

**4.** Guardá el archivo. `.env` ya está en `.gitignore` — no se sube a git, así
que no hay riesgo de commitearlo por error.

**5.** Levantá la app como siempre:

```bash
docker compose up --build
```

## 5. La regla corta

> Cada uno genera el suyo, en su compu, con el comando de su sistema. No se lo
> pasás a nadie, no le pedís el de nadie, y no importa que sean todos distintos.
