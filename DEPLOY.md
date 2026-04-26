# Guía de despliegue

Despliegue del proyecto desde cero, asumiendo que partes de un fork limpio de este repositorio. Coste: **0 €**.

**Stack final:**
- Base de datos: TiDB Cloud Serverless (MySQL-compatible, free tier)
- Backend: Render (Docker web service, free tier)
- Frontend: GitHub Pages (servicio estático)

**Tiempo estimado:** 30–45 minutos la primera vez.

---

## Prerrequisitos

- Cuenta en [GitHub](https://github.com)
- Cuenta en [TiDB Cloud](https://tidbcloud.com) (puedes registrarte con GitHub)
- Cuenta en [Render](https://render.com)
- Git instalado en local
- (Opcional) cliente MySQL — `mysql` CLI, MySQL Workbench, DBeaver, o el SQL Editor del propio TiDB Cloud

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

## 2. Crear el cluster en TiDB Serverless

1. Login en [tidbcloud.com](https://tidbcloud.com) (con GitHub o email).
2. **Create Cluster** → tipo **Serverless** → región más cercana al backend que vas a usar (si Render Frankfurt, elige `eu-central-1`; si Render US/SA, `us-east-1` o `us-west-2`).
3. Nombre libre, plan **Free**. **Create Cluster**.
4. Una vez creado, click en el cluster → botón **Connect** (arriba a la derecha) → pestaña **General** → en el dropdown **Connect With** elige **General**.
5. **Anota tres datos** (vas a necesitarlos en el paso 4):
   - **Host + puerto**: algo como `gateway01.us-east-1.prod.aws.tidbcloud.com:4000`
   - **Usuario**: incluye un prefijo del cluster, formato `<cluster_id>.<usuario>` (ej. `2U3ATWFK5HzW6Tk.root`)
   - **Password**: si es la primera vez, TiDB te ofrece **Generate Password** y la muestra una sola vez. Anótala.

> **Seguridad**: si en algún momento esa password queda expuesta, regénerala desde el botón **Reset Password** en el mismo diálogo de Connect. La password vieja queda invalidada al instante.

### 2.1. Crear la base de datos `products`

Por defecto TiDB conecta a `sys` (sistema). Necesitas una base dedicada.

1. En TiDB Cloud → tu cluster → menú lateral **SQL Editor** (también llamado **Chat2Query** en algunas regiones).
2. Ejecuta:
   ```sql
   CREATE DATABASE products;
   ```
3. Verifica:
   ```sql
   SHOW DATABASES;
   ```
   Debe aparecer `products` en la lista.

---

## 3. Cargar el esquema y los datos de demo

El backend, al arrancar por primera vez, ejecutará Flyway y creará el esquema vacío automáticamente (migraciones `V1` y `V2` en `server/src/main/resources/db/migration/`). Si quieres tener productos, pedales y amplificadores de demo desde el inicio, carga `server/products.sql`.

**Tres formas de hacerlo. La A es la más fácil.**

### A) SQL Editor de TiDB Cloud (recomendada, sin instalar nada)

1. TiDB Cloud → tu cluster → **SQL Editor**.
2. Selecciona la database `products` en el dropdown superior (no `sys`).
3. Antes de cargar nada, comprueba si ya hay datos:
   ```sql
   SELECT count(*) FROM product;
   ```
   - Si la tabla no existe (`Table 'products.product' doesn't exist`) → ve directo al paso 4. El propio script crea las tablas.
   - Si existe y devuelve `0` → ve al paso 4.
   - Si devuelve `>0` → el script las trunca antes de insertar (con `FOREIGN_KEY_CHECKS=0`), no necesitas hacer nada extra.
4. Abre `server/products.sql` en tu editor local, copia todo el contenido.
5. Pégalo en el SQL Editor de TiDB y dale a **Run**.
6. Verifica:
   ```sql
   SELECT count(*) FROM product;
   SELECT nombre, categoria, stock FROM product ORDER BY id LIMIT 5;
   ```
   Deberías ver 14 productos entre guitarras, pedales y amplificadores. **No esperes IDs `1,2,3...`** — TiDB asigna IDs en lotes (gaps de hasta 30000 entre sesiones); el primer producto puede tener `id=30001`. Es normal y no afecta a la app.

### B) `mysql` CLI desde la terminal

Requiere cliente MySQL instalado (Windows: `winget install Oracle.MySQL` o el "MySQL Shell"; macOS: `brew install mysql-client`).

```bash
mysql -h gateway01.us-east-1.prod.aws.tidbcloud.com -P 4000 \
      -u "<CLUSTER_ID>.root" -p \
      --ssl-mode=VERIFY_IDENTITY \
      products < server/products.sql
```

(Te pedirá la password interactivamente. El flag `--ssl-mode=VERIFY_IDENTITY` es obligatorio en TiDB Serverless.)

### C) Cliente gráfico (MySQL Workbench, DBeaver, TablePlus)

Conecta usando los datos del paso 2.5, marca **SSL: Require** o **VERIFY_IDENTITY** (varía por cliente), abre `server/products.sql` y ejecútalo entero.

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

   | Variable        | Valor                                                                                                                                  |
   |-----------------|----------------------------------------------------------------------------------------------------------------------------------------|
   | `DB_URL`        | `jdbc:mysql://<HOST>:4000/products?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3` (sustituye `<HOST>` por el de TiDB)    |
   | `DB_USERNAME`   | `<CLUSTER_ID>.root` (el usuario completo con el prefijo del cluster)                                                                   |
   | `DB_PASSWORD`   | Tu password de TiDB                                                                                                                    |
   | `CORS_ORIGINS`  | `http://localhost:5173` por ahora — lo cambiaremos al final cuando tengas la URL de GitHub Pages                                       |

   Detalles importantes sobre `DB_URL`:
   - **Lleva el prefijo `jdbc:`** delante de `mysql://...`
   - **Termina en `/products`**, NO en `/sys`
   - **No incluye usuario ni contraseña en la URL** — van separados en `DB_USERNAME` y `DB_PASSWORD`
   - **`sslMode=VERIFY_IDENTITY`** es obligatorio (TiDB rechaza conexiones sin TLS). Si por algún motivo da problemas con la cadena de CAs, prueba `sslMode=REQUIRED` (menos estricto pero igualmente cifrado).
   - **`enabledTLSProtocols=TLSv1.2,TLSv1.3`** — TiDB no acepta versiones más antiguas de TLS.

5. **Apply**. Render arranca el primer build (descarga deps de Maven + builda Docker, ~5–10 min).
6. Cuando termine, el servicio queda **Live** (verde, arriba a la derecha) y Render te da una URL del estilo `https://products-api-xxxx.onrender.com`. Anótala.

### 4.3. Verificar que el backend arrancó

```bash
curl https://products-api-xxxx.onrender.com/actuator/health
# Esperado: {"groups":["liveness","readiness"],"status":"UP"}

curl https://products-api-xxxx.onrender.com/api/v1/products
# Esperado: {"content":[{"id":30001,"name":"Fender Stratocaster ...
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

**`Communications link failure` o `SSL handshake error` al arrancar contra TiDB**
- Causa: la URL de conexión no incluye los parámetros TLS que TiDB exige.
- Fix: confirma que `DB_URL` contiene `sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3`. Si `VERIFY_IDENTITY` falla con un error de CA cert, baja a `sslMode=REQUIRED` — sigue cifrado, sólo no valida la cadena de certificados.

**`Unknown database 'sys'` o `Unknown database 'products'`**
- Causa: la URL apunta a `sys` (sistema, no debes usarla) o no creaste la base `products` en TiDB.
- Fix: ejecuta `CREATE DATABASE products;` en el SQL Editor (paso 2.1) y asegúrate de que `DB_URL` termina en `/products`.

**`Access denied for user 'root'@'%'`**
- Causa: el usuario no incluye el prefijo del cluster.
- Fix: TiDB Serverless requiere usuario con formato `<CLUSTER_ID>.root`, no `root` a secas. El cluster ID lo ves en el diálogo Connect.

**`AggregatedClassLoader.findClass` en el stack al arrancar la app**
- Causa: alguna línea del `application.properties` referencia una clase de Hibernate por nombre corto (sin paquete). Hibernate 7 (Spring Boot 4) exige FQN.
- Fix: si tienes `spring.jpa.properties.hibernate.dialect=...` con un nombre sin paquete, **bórrala**. Spring Boot 4 + driver MySQL auto-detecta el dialect correcto.

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

### Comportamiento específico de TiDB

**Los IDs no son consecutivos (`1, 2, 3, ...`) sino con saltos enormes (`30001, 30002, 60001, ...`)**
- Causa: comportamiento normal y esperado en TiDB. Cada sesión reserva un lote de 30000 IDs por adelantado para evitar contención en cluster distribuido.
- Fix: ninguno necesario. La aplicación trata los IDs como opacos, no asume continuidad. Si lo ves raro en la BD, ignóralo.

**Cold start lento la primera vez tras inactividad**
- Causa: TiDB Serverless free tier escala a cero tras inactividad. La primera conexión tras dormir tarda 5–15 segundos en levantar el cluster.
- Fix: el `connection-timeout=30000` en `application.properties` lo absorbe. Si necesitas latencia constante, mantén el cluster vivo con un cron externo (UptimeRobot pegando a `/actuator/health` cada 10 min).

---

## Costes y limitaciones del plan gratuito

- **TiDB Serverless free**: 5 GB de storage + 50M Request Units/mes. Auto-suspende tras inactividad (cold start ~5–15 s al despertar). MySQL-wire-compatible: cualquier driver MySQL estándar funciona.
- **Render free**: el servicio se suspende tras inactividad. El primer request tras dormir tarda ~30 s (Spring Boot tiene que arrancar). Si necesitas latencia constante, configura un cron externo (UptimeRobot) que pegue a `/actuator/health` cada 10 minutos.
- **GitHub Pages**: sin límites prácticos para uso personal (100 GB/mes blandos), pero sin previews por PR ni headers HTTP custom. Si te interesan esas features, considera Vercel o Cloudflare Pages.

---

## Variables de entorno (referencia rápida)

| Variable           | Dónde         | Descripción                                                                  | Default local                                  |
|--------------------|---------------|------------------------------------------------------------------------------|------------------------------------------------|
| `DB_URL`           | Render        | JDBC URL de MySQL/TiDB (con prefijo `jdbc:` y params TLS)                    | `jdbc:mysql://localhost:3306/products`         |
| `DB_USERNAME`      | Render / .env | Usuario de la BD (en TiDB: `<CLUSTER_ID>.root`)                              | `root`                                         |
| `DB_PASSWORD`      | Render / .env | Password de la BD                                                            | `root`                                         |
| `CORS_ORIGINS`     | Render        | Orígenes permitidos (coma-separados)                                         | `http://localhost:5173,http://localhost:4173`  |
| `SHOW_SQL`         | Render        | Log de SQL de Hibernate (true/false)                                         | `false`                                        |
| `PORT`             | Render        | Puerto HTTP (Render lo inyecta automáticamente)                              | `8080`                                         |
| `VITE_API_URL`     | GitHub Actions| URL base del backend (sin `/api/v1`)                                         | `http://localhost:8080`                        |
