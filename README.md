# Productos — E-commerce de instrumentos musicales

Aplicación full-stack para gestión de un catálogo de productos (guitarras eléctricas, pedales y amplificadores valvulares) y procesamiento de pedidos.

- **Backend**: Java 21 · Spring Boot 4 · Spring Data JPA · Flyway · Spring Security · Caffeine · OpenAPI
- **Frontend**: React 19 · Vite · React Router · React Bootstrap · Axios
- **Base de datos**: PostgreSQL 16 (Neon en producción, Docker en local)
- **Despliegue**: Render (web service Docker) + Neon (PostgreSQL serverless)

## Estructura del repositorio

```
productos/
├── render.yaml        # IaC para Render (en la raíz del repo)
├── client/            # Frontend React + Vite
└── server/            # Backend Spring Boot
    ├── src/main/java/com/josefuentes4096/products/
    │   ├── controller/    # REST controllers (productos y pedidos)
    │   ├── service/       # Lógica de negocio
    │   ├── repository/    # Spring Data JPA
    │   ├── entity/        # JPA entities
    │   ├── dto/           # DTOs de request/response
    │   ├── config/        # CORS, Security, Cache, OpenAPI, JPA Auditing
    │   └── exception/     # Manejo global de errores
    ├── src/main/resources/
    │   ├── application.properties
    │   └── db/migration/  # Flyway (V1 schema, V2 seed)
    ├── Dockerfile         # Multi-stage build
    ├── docker-compose.yml # Postgres + app para desarrollo local
    └── products.sql       # Schema + seed de demo
```

## Endpoints principales

Base URL: `/api/v1`

| Método | Ruta                              | Descripción                          |
|--------|-----------------------------------|--------------------------------------|
| GET    | `/products`                       | Listar productos                     |
| GET    | `/products/{id}`                  | Detalle de un producto               |
| POST   | `/products`                       | Crear producto                       |
| PUT    | `/products/{id}`                  | Actualizar producto                  |
| DELETE | `/products/{id}`                  | Borrar producto                      |
| GET    | `/products/search?q=...`          | Buscar por nombre                    |
| GET    | `/products/category/{category}`   | Filtrar por categoría                |
| GET    | `/products/low-stock`             | Productos por debajo del stock mínimo |
| POST   | `/orders`                         | Crear pedido                         |
| GET    | `/orders/user/{userId}`           | Pedidos de un usuario                |

Documentación interactiva: `/swagger-ui/index.html` · Health: `/actuator/health`

## Desarrollo local

### Backend (Docker Compose: Postgres + app)

```bash
cd server
cp .env.example .env   # o crea uno con DB_USERNAME / DB_PASSWORD
docker compose up --build
```

App en `http://localhost:8080`, Postgres en `localhost:5432`.

### Backend (sin Docker)

Necesitas Postgres 16 corriendo localmente con una base `products`:

```bash
cd server
./mvnw spring-boot:run
```

### Frontend

```bash
cd client
npm install
npm run dev
```

Vite sirve en `http://localhost:5173`.

### Tests

```bash
# Backend (JUnit + JaCoCo, mínimo 80% líneas)
cd server && ./mvnw test

# Frontend (Vitest + Testing Library)
cd client && npm test
```

## Despliegue en producción (Render + Neon)

### 1. Crear la base de datos en Neon

1. Crea un proyecto en [neon.tech](https://neon.tech).
2. Copia la cadena de conexión JDBC y conviértela a formato Spring:
   `jdbc:postgresql://ep-xxx.region.aws.neon.tech/products?sslmode=require&channelBinding=require`
3. Anota usuario y password.

(Opcional) Carga los datos de demo desde tu máquina:

```bash
psql "postgresql://USER:PASS@ep-xxx.region.aws.neon.tech/products?sslmode=require" \
  -f server/products.sql
```

Si no lo cargas, Flyway creará el schema vacío (V1) y el setting por defecto (V2) en el primer arranque.

### 2. Desplegar el backend en Render

Opción A — `render.yaml` (recomendado):

1. Sube el repo a GitHub.
2. En Render, crea un nuevo Blueprint apuntando a este repo.
3. Render detecta `render.yaml` (en la raíz, con `rootDir: server`) y crea el web service.
4. Configura las env vars marcadas `sync: false`:
   - `DB_URL` — cadena JDBC de Neon
   - `DB_USERNAME` — usuario de Neon
   - `DB_PASSWORD` — password de Neon
   - `CORS_ORIGINS` — URL del frontend desplegado (ej. `https://mi-app.vercel.app`)

Opción B — manual:

1. New → Web Service → Docker → conecta el repo.
2. Root Directory: `server`.
3. Health Check Path: `/actuator/health`.
4. Añade las mismas env vars del punto anterior.

### 3. Desplegar el frontend

Cualquier hosting estático (Vercel, Netlify, Cloudflare Pages, GitHub Pages):

```bash
cd client
npm run build   # genera dist/
```

Configura la URL del backend en el `.env` del cliente antes de buildear (sin sufijo `/api/v1`, el cliente lo añade):

```
VITE_API_URL=https://products-api.onrender.com
```

## Notas sobre el plan gratuito

- **Render free** suspende el servicio tras inactividad: el primer request tras dormir tarda ~30 s. Si necesitas latencia constante, usa un cron externo (UptimeRobot) que pegue a `/actuator/health` cada 10 minutos.
- **Neon free** también auto-suspende el endpoint. El driver puede dar timeout en frío; el `connection-timeout=30000` ya configurado en `application.properties` lo absorbe.
- Neon **exige TLS**: `sslmode=require` es obligatorio en la URL.

## Variables de entorno (resumen)

| Variable           | Dónde         | Descripción                                            | Default local                              |
|--------------------|---------------|--------------------------------------------------------|--------------------------------------------|
| `DB_URL`           | server        | JDBC URL de Postgres                                   | `jdbc:postgresql://localhost:5432/products`|
| `DB_USERNAME`      | server        | Usuario BD                                             | `postgres`                                 |
| `DB_PASSWORD`      | server        | Password BD                                            | `postgres`                                 |
| `CORS_ORIGINS`     | server        | Orígenes permitidos (coma-separados)                   | `http://localhost:5173,http://localhost:4173` |
| `SHOW_SQL`         | server        | Log de SQL de Hibernate                                | `false`                                    |
| `PORT`             | server        | Puerto HTTP (Render lo inyecta)                        | `8080`                                     |
| `VITE_API_URL`     | client        | URL base del backend (sin `/api/v1`)                   | `http://localhost:8080`                    |

## Licencia

MIT — ver [`server/LICENSE`](server/LICENSE).
