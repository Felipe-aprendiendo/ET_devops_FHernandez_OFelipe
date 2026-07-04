# Backend Despachos — Sistema Ventas-Despacho (Innovatech Chile)

## ¿Qué hace este servicio?

API REST que gestiona las órdenes de despacho. Expone los endpoints bajo
`/api/v1/despachos` y persiste los datos en una base de datos MySQL externa (RDS).

**Stack:** Spring Boot 3.4.4, Java 17, Spring Data JPA, MySQL Connector/J
**Puerto:** 8081 (fijado explícitamente vía `server.port=8081` en `application.properties`)
**Módulo:** `back-Despachos_SpringBoot/Springboot-API-REST-DESPACHO/`

---

## Cómo correrlo localmente

```bash
# Desde back-Despachos_SpringBoot/Springboot-API-REST-DESPACHO/
export DB_ENDPOINT=localhost
export DB_PORT=3306
export DB_NAME=despachos_db
export DB_USERNAME=root
export DB_PASSWORD=tu_password

./mvnw clean package -DskipTests
java -jar target/Springboot-API-REST-DESPACHO-*.jar
```

La API queda disponible en `http://localhost:8081/api/v1/despachos`.

---

## Variables de entorno requeridas

| Variable | Descripción |
|----------|-------------|
| `DB_ENDPOINT` | Host de la instancia MySQL / RDS (sin puerto) |
| `DB_PORT` | Puerto de MySQL (normalmente `3306`) |
| `DB_NAME` | Nombre del schema / base de datos (`despachos_db`) |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |

En Kubernetes estas variables se inyectan automáticamente desde el Secret
`db-credentials-despachos` definido en `k8s/02-secret-db-despachos.yaml`.

---

## Docker — build y ejecución local

```bash
# Build (desde back-Despachos_SpringBoot/Springboot-API-REST-DESPACHO/)
docker build -t backend-despachos:local .

# Ejecutar con variables de entorno
docker run -p 8081:8081 \
  -e DB_ENDPOINT=localhost \
  -e DB_PORT=3306 \
  -e DB_NAME=despachos_db \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=tu_password \
  backend-despachos:local
```

---

## Pipeline CI/CD

**Archivo:** `.github/workflows/deploy-backend-despachos.yml`

| Paso | Qué hace |
|------|----------|
| Trigger | Push a la rama `deploy` con cambios en `back-Despachos_SpringBoot/**` |
| Build | `docker build` multi-stage (Maven → JRE Alpine) |
| Push | Sube imagen a ECR (`sistema-despachos-despachos`) con tag `${{ github.sha }}` |
| Deploy | `kubectl set image` + `kubectl rollout status` en namespace `sistema-despachos` |

---

## Despliegue en EKS

A diferencia de EP2 (instancia EC2 con Docker Compose), en EP3 este servicio corre
como un Deployment de Kubernetes con HPA (autoscaling por CPU). El Service es de
tipo `ClusterIP` — no se expone directamente a Internet; solo el frontend (Nginx)
puede llamarlo internamente.

Manifiestos: [`k8s/20-backend-despachos-deployment.yaml`](../k8s/20-backend-despachos-deployment.yaml), [`k8s/21-backend-despachos-service.yaml`](../k8s/21-backend-despachos-service.yaml), [`k8s/22-backend-despachos-hpa.yaml`](../k8s/22-backend-despachos-hpa.yaml)
