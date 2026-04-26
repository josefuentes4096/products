# Productos — E-commerce de instrumentos musicales

Aplicación full-stack para gestión de un catálogo de productos (guitarras eléctricas, pedales y amplificadores valvulares) y procesamiento de pedidos.

- **Backend**: Java 21 · Spring Boot 4 · Spring Data JPA · Flyway · Spring Security · Caffeine · OpenAPI
- **Frontend**: React 19 · Vite · React Router · React Bootstrap · Axios
- **Base de datos**: MySQL 8 / TiDB Serverless (MySQL-compatible) · TiDB Cloud en producción, Docker MySQL en local
- **Despliegue**: Render (backend Docker) + TiDB Cloud (BD serverless) + GitHub Pages (frontend)

> Para la guía completa de despliegue desde cero (con troubleshooting de los problemas más comunes), ver **[`DEPLOY.md`](DEPLOY.md)**.

## Demo en vivo

- **Frontend**: https://josefuentes4096.github.io/products/
- **API**: https://products-k2x6.onrender.com/api/v1/products
- **Docs interactivas (Swagger UI)**: https://products-k2x6.onrender.com/swagger-ui/index.html
- **Healthcheck**: https://products-k2x6.onrender.com/actuator/health

> Estas URLs corresponden al despliegue del fork original. Si haces tu propio fork, tendrás URLs distintas — ver [`DEPLOY.md`](DEPLOY.md).
>
> Nota: ambos servicios están en plan free y se suspenden tras inactividad. El primer request tras dormir tarda **~30 s en Render + 5–15 s en TiDB**, hasta ~45 s en total. Insiste si no responde a la primera.

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
    ├── docker-compose.yml # MySQL + app para desarrollo local
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

### Backend (Docker Compose: MySQL + app)

```bash
cd server
cp .env.example .env   # o crea uno con DB_PASSWORD
docker compose up --build
```

App en `http://localhost:8080`, MySQL en `localhost:3306` (base `products`, usuario `root`).

### Backend (sin Docker)

Necesitas MySQL 8 corriendo localmente con una base `products`:

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

## Despliegue en producción

El paso a paso completo (con troubleshooting de los problemas reales que aparecen con TiDB, Render y GitHub Pages) está en **[`DEPLOY.md`](DEPLOY.md)**. Resumen del flujo:

1. **TiDB Cloud** — crear cluster Serverless, crear la base `products`, anotar host/usuario/password.
2. **Render** — conectar la GitHub App al fork, crear Blueprint, rellenar `DB_URL` (formato `jdbc:mysql://<host>:4000/products?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3`), `DB_USERNAME` (con prefijo `<CLUSTER_ID>.root`), `DB_PASSWORD`, `CORS_ORIGINS`.
3. **GitHub Pages** — Settings → Pages → Source: GitHub Actions, crear repo variable `VITE_API_URL` con la URL de Render, push.
4. **CORS** — añadir `https://<TU-USUARIO>.github.io` a `CORS_ORIGINS` en Render.

(Opcional) Cargar los datos de demo en TiDB desde el SQL Editor de TiDB Cloud o con cliente MySQL:

```bash
mysql -h <HOST> -P 4000 -u "<CLUSTER_ID>.root" -p \
      --ssl-mode=VERIFY_IDENTITY \
      products < server/products.sql
```

Si no lo cargas, Flyway crea el schema vacío (V1) y el setting por defecto (V2) en el primer arranque.

## Notas sobre el plan gratuito

- **Render free** suspende el servicio tras inactividad: el primer request tras dormir tarda ~30 s. Si necesitas latencia constante, usa un cron externo (UptimeRobot) que pegue a `/actuator/health` cada 10 minutos.
- **TiDB Serverless free** también auto-suspende (cold start ~5–15 s al despertar). El `connection-timeout=30000` ya configurado en `application.properties` lo absorbe. Cuota: 5 GB storage + 50M Request Units/mes.
- TiDB **exige TLS**: `sslMode=VERIFY_IDENTITY` (o como mínimo `REQUIRED`) es obligatorio en la URL JDBC.
- TiDB asigna IDs en lotes de 30000; no esperes valores contiguos como `1, 2, 3` (la app trata IDs como opacos, no afecta).

## Variables de entorno (resumen)

| Variable           | Dónde         | Descripción                                            | Default local                              |
|--------------------|---------------|--------------------------------------------------------|--------------------------------------------|
| `DB_URL`           | server        | JDBC URL de MySQL/TiDB                                 | `jdbc:mysql://localhost:3306/products`     |
| `DB_USERNAME`      | server        | Usuario BD (en TiDB: `<CLUSTER_ID>.root`)              | `root`                                     |
| `DB_PASSWORD`      | server        | Password BD                                            | `root`                                     |
| `CORS_ORIGINS`     | server        | Orígenes permitidos (coma-separados)                   | `http://localhost:5173,http://localhost:4173` |
| `SHOW_SQL`         | server        | Log de SQL de Hibernate                                | `false`                                    |
| `PORT`             | server        | Puerto HTTP (Render lo inyecta)                        | `8080`                                     |
| `VITE_API_URL`     | client        | URL base del backend (sin `/api/v1`)                   | `http://localhost:8080`                    |

## Licencia

MIT — ver [`server/LICENSE`](server/LICENSE).
