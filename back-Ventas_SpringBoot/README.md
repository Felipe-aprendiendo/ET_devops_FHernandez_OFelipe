# Backend Ventas — Sistema Ventas-Despacho (Innovatech Chile)

## ¿Qué hace este servicio?

API REST que gestiona las órdenes de compra (ventas). Expone los endpoints
bajo `/api/v1/ventas` y persiste los datos en una base de datos MySQL externa (RDS).

**Stack:** Spring Boot 3.4.4, Java 17, Spring Data JPA, MySQL Connector/J
**Puerto:** 8080 (default de Spring Boot)
**Módulo:** `back-Ventas_SpringBoot/Springboot-API-REST/`

---

## Cómo correrlo localmente

```bash
# Desde back-Ventas_SpringBoot/Springboot-API-REST/
export DB_ENDPOINT=localhost
export DB_PORT=3306
export DB_NAME=ventas_db
export DB_USERNAME=root
export DB_PASSWORD=tu_password

./mvnw clean package -DskipTests
java -jar target/Springboot-API-REST-*.jar
```

La API queda disponible en `http://localhost:8080/api/v1/ventas`.

---

## Variables de entorno requeridas

| Variable | Descripción |
|----------|-------------|
| `DB_ENDPOINT` | Host de la instancia MySQL / RDS (sin puerto) |
| `DB_PORT` | Puerto de MySQL (normalmente `3306`) |
| `DB_NAME` | Nombre del schema / base de datos (`ventas_db`) |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |

En Kubernetes estas variables se inyectan automáticamente desde el Secret
`db-credentials-ventas` definido en `k8s/01-secret-db-ventas.yaml`.

---

## Docker — build y ejecución local

```bash
# Build (desde back-Ventas_SpringBoot/Springboot-API-REST/)
docker build -t backend-ventas:local .

# Ejecutar con variables de entorno
docker run -p 8080:8080 \
  -e DB_ENDPOINT=localhost \
  -e DB_PORT=3306 \
  -e DB_NAME=ventas_db \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=tu_password \
  backend-ventas:local
```

---

## Pipeline CI/CD

**Archivo:** `.github/workflows/deploy-backend-ventas.yml`

| Paso | Qué hace |
|------|----------|
| Trigger | Push a la rama `deploy` con cambios en `back-Ventas_SpringBoot/**` |
| Build | `docker build` multi-stage (Maven → JRE Alpine) |
| Push | Sube imagen a ECR (`sistema-despachos-ventas`) con tag `${{ github.sha }}` |
| Deploy | `kubectl set image` + `kubectl rollout status` en namespace `sistema-despachos` |

---

## Despliegue en EKS

A diferencia de EP2 (instancia EC2 con Docker Compose), en EP3 este servicio corre
como un Deployment de Kubernetes con HPA (autoscaling por CPU). El Service es de
tipo `ClusterIP` — no se expone directamente a Internet; solo el frontend (Nginx)
puede llamarlo internamente.

Manifiestos: [`k8s/10-backend-ventas-deployment.yaml`](../k8s/10-backend-ventas-deployment.yaml), [`k8s/11-backend-ventas-service.yaml`](../k8s/11-backend-ventas-service.yaml), [`k8s/12-backend-ventas-hpa.yaml`](../k8s/12-backend-ventas-hpa.yaml)
# trigger inicial de pipeline Sun Jul  5 03:04:18 UTC 2026
