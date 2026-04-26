# Guía de despliegue

Despliegue del proyecto desde cero, asumiendo que partes de un fork limpio de este repositorio. Coste: **0 €**.

**Stack final:**
- Base de datos: Neon (PostgreSQL serverless, free)
- Backend: Render (Docker web service, free)
- Frontend: GitHub Pages (servicio estático)

**Tiempo estimado:** 30–45 minutos la primera vez.

---

## Prerrequisitos

- Cuenta en [GitHub](https://github.com)
- Cuenta en [Neon](https://neon.tech)
- Cuenta en [Render](https://render.com)
- Git instalado en local
- (Opcional) `psql` o cliente PostgreSQL si prefieres cargar la BD por terminal en lugar de por web

---

## 1. Fork del repositorio

1. En GitHub, dale al botón **Fork** sobre este repo. Acabarás con `https://github.com/<TU-USUARIO>/products`.
2. Clona en local:
   ```bash
   git clone https://github.com/<TU-USUARIO>/products.git
   cd products
   ```

> **Importante**: si renombras el repo a algo distinto de `products`, tendrás que ajustar `client/vite.config.js` (`base`) y `client/src/App.jsx` (`basename`) — ver paso 5.

---

## 2. Crear la base de datos en Neon

1. Login en [console.neon.tech](https://console.neon.tech).
2. **Create project**: nombre libre, región la más cercana a tu backend (si vas a usar Render Frankfurt, elige una región europea o de Sudamérica como `sa-east-1`).
3. Una vez creado, en el panel del proyecto verás una **Connection string** parecida a:
   ```
   postgresql://neondb_owner:npg_XXXXXXXXXXXX@ep-abc-def-pooler.region.aws.neon.tech/neondb?sslmode=require
   ```
4. **Anota tres datos** (vas a necesitarlos en el paso 4):
   - **Host + database + params**: `ep-abc-def-pooler.region.aws.neon.tech/neondb?sslmode=require&channelBinding=require`
     - Si la URL de Neon no incluye `channelBinding=require`, añádelo a mano. Neon lo recomienda para mayor seguridad.
   - **Usuario**: `neondb_owner` (suele ser este por defecto)
   - **Contraseña**: la cadena que empieza por `npg_...`

> **Seguridad**: si en algún momento esa contraseña queda expuesta (commit, log, captura), rótala desde Neon → **Roles** → tu rol → **Reset password**. La contraseña vieja queda invalidada al instante.

---

## 3. Cargar el esquema y los datos de demo

El backend, al arrancar por primera vez, ejecutará Flyway y creará el esquema vacío automáticamente (migraciones `V1` y `V2` en `server/src/main/resources/db/migration/`). Si quieres tener productos, pedales y amplificadores de demo desde el inicio, carga `server/products.sql`.

**Tres formas de hacerlo. La A es la más fácil.**

### A) Neon SQL Editor (recomendada, sin instalar nada)

1. Neon dashboard → tu proyecto → menú lateral **SQL Editor**.
2. Selecciona el branch (`production` por defecto) y la database (`neondb`).
3. Antes de cargar nada, comprueba si ya hay datos:
   ```sql
   SELECT count(*) FROM product;
   ```
   - Si la tabla no existe → ejecuta `products.sql` directo (paso 5).
   - Si existe y devuelve `0` → carga `products.sql`.
   - Si devuelve `>0` → primero limpia para no duplicar:
     ```sql
     TRUNCATE order_item, orders, product RESTART IDENTITY CASCADE;
     ```
4. Abre `server/products.sql` en tu editor local, copia todo el contenido (81 líneas).
5. Pégalo en el SQL Editor de Neon y dale a **Run**.
6. Verifica:
   ```sql
   SELECT count(*) FROM product;
   SELECT nombre, categoria, stock FROM product ORDER BY id LIMIT 5;
   ```
   Deberías ver 14 productos entre guitarras, pedales y amplificadores.

### B) `psql` desde la terminal

Requiere PostgreSQL client instalado (Windows: `winget install PostgreSQL.PostgreSQL` y marca "Command Line Tools" en el wizard).

```bash
psql "postgresql://neondb_owner:<TU-PASS>@ep-abc-def-pooler.region.aws.neon.tech/neondb?sslmode=require" \
  -f server/products.sql
```

Las comillas son obligatorias (la URL contiene caracteres especiales).

### C) Neon CLI

```bash
npm install -g neonctl
neonctl auth
neonctl sql --project-id <TU-PROJECT-ID> -f server/products.sql
```

---

## 4. Desplegar el backend en Render

### 4.1. Conectar la GitHub App de Render a tu fork

**Esto es crítico hacerlo ANTES de crear el servicio.** Si no, Render hará un clone anónimo y creará el servicio con configuración por defecto, ignorando `render.yaml`.

1. Login en [dashboard.render.com](https://dashboard.render.com).
2. Esquina inferior izquierda → click en tu avatar → **Account Settings**.
3. Menú lateral → **GitHub**.
4. Click en **Configure account** junto a tu usuario. Te lleva a GitHub.
5. En GitHub, en "Repository access" → **Only select repositories** → busca y marca tu fork `products` → **Save**.
6. Vuelve a Render.

### 4.2. Crear el Blueprint

1. **New +** (arriba a la derecha) → **Blueprint**.
2. Selecciona tu fork `<TU-USUARIO>/products` (debería aparecer en la lista; si no, repite 4.1).
3. Render detecta `render.yaml` y propone crear `products-api` (Docker, root `server/`, region Frankfurt, plan free, healthcheck `/actuator/health`).
4. Te pedirá rellenar las 4 env vars marcadas como `sync: false`:

   | Variable        | Valor                                                                                                  |
   |-----------------|--------------------------------------------------------------------------------------------------------|
   | `DB_URL`        | `jdbc:postgresql://ep-abc-def-pooler.region.aws.neon.tech/neondb?sslmode=require&channelBinding=require` (host de tu Neon, con prefijo `jdbc:`) |
   | `DB_USERNAME`   | `neondb_owner` (o el usuario que te dé Neon)                                                           |
   | `DB_PASSWORD`   | Tu password de Neon (el `npg_...`)                                                                     |
   | `CORS_ORIGINS`  | `http://localhost:5173` por ahora — lo cambiaremos al final cuando tengas la URL de GitHub Pages       |

   Detalles importantes sobre `DB_URL`:
   - **Lleva el prefijo `jdbc:`** delante de `postgresql://...`
   - **No incluye usuario ni contraseña en la URL** — van separados en `DB_USERNAME` y `DB_PASSWORD`
   - **Mantén `?sslmode=require&channelBinding=require`** (Neon exige TLS)

5. **Apply**. Render arranca el primer build (descarga deps de Maven + builda Docker, ~5–10 min).
6. Cuando termine, el servicio queda **Live** (verde, arriba a la derecha) y Render te da una URL del estilo `https://products-api-xxxx.onrender.com`. Anótala.

### 4.3. Verificar que el backend arrancó

```bash
curl https://products-api-xxxx.onrender.com/actuator/health
# Esperado: {"groups":["liveness","readiness"],"status":"UP"}

curl https://products-api-xxxx.onrender.com/api/v1/products
# Esperado: {"content":[{"id":1,"name":"Fender Stratocaster ...
```

Si algo va mal aquí, salta al apartado **Troubleshooting** al final.

---

## 5. Desplegar el frontend en GitHub Pages

### 5.1. Ajustar el base path si renombraste el repo

Si tu fork se llama `products` (igual que el original), **salta al paso 5.2**. Si lo renombraste:

1. Edita `client/vite.config.js`:
   ```js
   base: '/<NOMBRE-DE-TU-REPO>/',
   ```
2. Edita `client/src/App.jsx`:
   ```jsx
   <BrowserRouter basename="/<NOMBRE-DE-TU-REPO>">
   ```

Ambos deben coincidir y deben coincidir con el subpath que GitHub Pages servirá: `https://<TU-USUARIO>.github.io/<NOMBRE-DE-TU-REPO>/`.

### 5.2. Configurar GitHub Pages source

1. En GitHub: tu fork → **Settings** → menú lateral **Pages**.
2. **Source**: selecciona **"GitHub Actions"** (no "Deploy from a branch").
3. No hace falta "Save", se aplica al instante.

### 5.3. Crear la repo variable VITE_API_URL

El workflow (`.github/workflows/deploy-pages.yml`) inyecta esta variable durante el build de Vite:

1. GitHub: tu fork → **Settings** → **Secrets and variables** → **Actions**.
2. Pestaña **Variables** (no "Secrets") → **New repository variable**.
3. Name: `VITE_API_URL` · Value: la URL de Render del paso 4.3 (ejemplo: `https://products-api-xxxx.onrender.com`).
   - **Sin barra final**.
   - **Sin `/api/v1`** — el cliente axios lo añade.

### 5.4. Push para disparar el primer deploy

Si has tocado los archivos del paso 5.1, commitealos primero. Después:

```bash
git push origin main
```

Mira la pestaña **Actions** del repo → verás el workflow "Deploy frontend to GitHub Pages" corriendo. Tarda ~2–3 min.

Cuando termine en verde, tu app está en:
```
https://<TU-USUARIO>.github.io/<NOMBRE-DEL-REPO>/
```

---

## 6. Cerrar el bucle: actualizar CORS

Si abres ahora la URL de GitHub Pages, la home cargará pero **el listado de productos vendrá vacío** y verás errores de CORS en la consola del navegador. Toca actualizar la lista de orígenes permitidos en el backend:

1. Render → tu servicio `products-api` → **Environment**.
2. Edita `CORS_ORIGINS`. Cambia el valor por:
   ```
   https://<TU-USUARIO>.github.io
   ```
   - **Sin barra final**.
   - **Sin el subpath** (`/products` o el nombre del repo). El navegador envía como `Origin` solo el dominio.
   - Si quieres mantener `localhost` para desarrollo local, separa por comas: `http://localhost:5173,https://<TU-USUARIO>.github.io`.
3. **Save Changes**. Render redespliega automáticamente (~1–2 min).

---

## 7. Verificación final

1. Abre `https://<TU-USUARIO>.github.io/<NOMBRE-DEL-REPO>/` → debe cargar la home con los productos.
2. Click en un producto → ficha de detalle.
3. **F5 en una ruta interna** (ej. `/cart` después de añadir algo). Si recarga sin 404, el truco del `404.html` funciona.
4. DevTools → Network → XHR → llamadas a `https://products-api-xxxx.onrender.com/api/v1/...` deben devolver `200`.

Si los 4 pasos pasan, deploy completo.

---

## Troubleshooting — problemas reales encontrados

### Backend en Render

**"It looks like we don't have access to your repo"** en los primeros logs del build
- Causa: la GitHub App de Render no tiene permiso sobre tu fork.
- Fix: paso 4.1. Después, **borra el servicio creado** (Settings → Delete Service) y rehaz el flujo Blueprint.

**`failed to read dockerfile: open Dockerfile: no such file or directory`** + `transferring dockerfile: 2B`
- Causa: el servicio se creó sin acceso a GitHub, así que ignoró `render.yaml` y Root Directory quedó vacío. Render busca el Dockerfile en la raíz del repo, donde no existe (vive en `server/Dockerfile`).
- Fix: la forma limpia es borrar el servicio y rehacer el Blueprint con la GitHub App ya conectada. Alternativa: ir a Settings del servicio existente, **Root Directory** → escribir `server`, **Dockerfile Path** → `./Dockerfile`, Save, Manual Deploy.

**`process "/bin/sh -c ./mvnw -B dependency:go-offline" did not complete successfully: exit code: 126`**
- Causa: `exit 126` = "permission denied". El script `mvnw` se subió a git desde Windows sin el bit ejecutable, y el contenedor Linux no puede ejecutarlo.
- Fix:
  ```bash
  git update-index --chmod=+x server/mvnw
  git commit -m "fix: mark mvnw as executable"
  git push origin main
  ```
  `git update-index --chmod=+x` cambia el modo en el índice de git sin tocar el archivo en disco — funciona perfecto desde Windows.

**`AggregatedClassLoader.findClass` en el stack al arrancar la app**
- Causa: alguna línea del `application.properties` referencia una clase de Hibernate por nombre corto (sin paquete). Hibernate 7 (Spring Boot 4) exige FQN.
- Fix más común: si tienes `spring.jpa.properties.hibernate.dialect=PostgreSQLDialect`, **bórrala**. Spring Boot 4 + driver PostgreSQL auto-detecta el dialect correcto.

### Push a GitHub

**`git push` se cuelga indefinidamente en Windows**
- Causa: Git Credential Manager está esperando reautenticación pero el popup OAuth se quedó escondido o la sesión caducó.
- Fix:
  ```bash
  cmdkey /delete:git:https://github.com
  git push -v origin main
  ```
  Eso fuerza un nuevo flujo OAuth que abrirá una pestaña en el navegador.

### Frontend en GitHub Pages

**El workflow termina verde pero la URL devuelve 404**
- Causa: `Settings → Pages → Source` no está en "GitHub Actions".
- Fix: paso 5.2.

**La página carga pero sin estilos / con JS roto**
- Causa: el `base` en `vite.config.js` no coincide con el subpath de la URL.
- Fix: paso 5.1 — `base` y `basename` deben ser exactamente el nombre del repo entre barras.

**F5 en una ruta interna devuelve 404**
- Causa: falta el truco del `404.html` (el workflow lo hace; si lo modificaste y quitaste la línea `cp dist/index.html dist/404.html`, GitHub Pages no tiene fallback).
- Fix: revisa `.github/workflows/deploy-pages.yml`, debe tener el step "SPA fallback".

**Errores de CORS en la consola del navegador**
- Causa: el dominio de GitHub Pages no está en `CORS_ORIGINS` del backend.
- Fix: paso 6. El valor debe ser exactamente `https://<TU-USUARIO>.github.io` (sin path, sin barra final).

**El listado de productos viene vacío**
- Causa: la BD está vacía. Flyway crea el esquema pero no carga datos demo.
- Fix: paso 3.

**Las imágenes no se muestran (placeholders no aparecen)**
- Causa: `placehold.co` (el host por defecto de los placeholders en `products.sql`) está en algunas listas de bloqueo de uBlock Origin / Brave Shields / AdGuard.
- Fix: prueba en modo incógnito o con bloqueadores desactivados. Si quieres cambiarlo permanentemente:
  ```sql
  UPDATE product
  SET imagen_url = REPLACE(imagen_url, 'placehold.co/400x300?text=', 'picsum.photos/seed/');
  ```

---

## Costes y limitaciones del plan gratuito

- **Render free**: el servicio se suspende tras inactividad. El primer request tras dormir tarda ~30 s (Spring Boot tiene que arrancar). Si necesitas latencia constante, configura un cron externo (UptimeRobot) que pegue a `/actuator/health` cada 10 minutos.
- **Neon free**: el endpoint también auto-suspende. El driver puede dar timeout en frío; el `connection-timeout=30000` en `application.properties` lo absorbe.
- **GitHub Pages**: sin límites prácticos para uso personal (100 GB/mes blandos), pero sin previews por PR ni headers HTTP custom. Si te interesan esas features, considera Vercel o Cloudflare Pages.

---

## Variables de entorno (referencia rápida)

| Variable           | Dónde         | Descripción                                                | Default local                                  |
|--------------------|---------------|------------------------------------------------------------|------------------------------------------------|
| `DB_URL`           | Render        | JDBC URL de Postgres (con prefijo `jdbc:`)                 | `jdbc:postgresql://localhost:5432/products`    |
| `DB_USERNAME`      | Render / .env | Usuario de la BD                                           | `postgres`                                     |
| `DB_PASSWORD`      | Render / .env | Password de la BD                                          | `postgres`                                     |
| `CORS_ORIGINS`     | Render        | Orígenes permitidos (coma-separados)                       | `http://localhost:5173,http://localhost:4173`  |
| `SHOW_SQL`         | Render        | Log de SQL de Hibernate (true/false)                       | `false`                                        |
| `PORT`             | Render        | Puerto HTTP (Render lo inyecta automáticamente)            | `8080`                                         |
| `VITE_API_URL`     | GitHub Actions| URL base del backend (sin `/api/v1`)                       | `http://localhost:8080`                        |
