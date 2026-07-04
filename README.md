# Sistema Ventas-Despacho (Innovatech Chile)

Monorepo del sistema de ventas y despacho: dos APIs REST en Spring Boot y un
frontend en React, con una base de datos MySQL compartida.

---

## ¿Qué es este proyecto?

Plataforma que permite gestionar ventas y despachos de forma independiente
pero integrada: cada dominio tiene su propio backend y su propia base de
datos, y el frontend consume ambos a través de un único punto de entrada
(proxy inverso de Nginx).

---

## Arquitectura

```
front_despacho (React + Vite, servido por Nginx)
        │
        ├── /api/ventas/     → backend-ventas     (Spring Boot, :8080)
        └── /api/despachos/  → backend-despachos  (Spring Boot, :8081)
                                        │
                                        └── mysql (:3306)
```

- **backend-ventas** — API REST de ventas ([back-Ventas_SpringBoot/](back-Ventas_SpringBoot/)), puerto 8080.
- **backend-despachos** — API REST de despachos ([back-Despachos_SpringBoot/](back-Despachos_SpringBoot/)), puerto 8081.
- **frontend** — SPA en React servida por Nginx ([front_despacho/](front_despacho/)), que además actúa como proxy inverso hacia ambos backends.
- **mysql** — Un único contenedor MySQL 8 compartido por ambos backends. Cada
  backend crea automáticamente su propia base (`ventas_db` / `despachos_db`)
  gracias a `createDatabaseIfNotExist=true`, usando el mismo usuario `root`.

Los nombres de servicio `backend-ventas` y `backend-despachos` son los mismos
tanto en `docker-compose.yml` como en Kubernetes, porque `nginx.conf` del
frontend hace `proxy_pass` a esos hostnames literalmente.

---

## Cómo levantar todo localmente (Docker Compose)

Requisitos: Docker y Docker Compose instalados.

```bash
# Desde la raíz del repositorio
docker compose up --build
```

Esto levanta:
- MySQL en el puerto `3306`
- backend-ventas y backend-despachos (esperan a que MySQL esté saludable antes de arrancar)
- El frontend en `http://localhost:8080`

Para detener y limpiar:

```bash
docker compose down
```

> Las contraseñas usadas en `docker-compose.yml` son valores de desarrollo
> simples, pensados solo para uso local. Nunca deben reutilizarse en producción.

---

## Estructura de carpetas

```
.
├── back-Ventas_SpringBoot/       # API REST de ventas (Spring Boot)
├── back-Despachos_SpringBoot/    # API REST de despachos (Spring Boot)
├── front_despacho/               # Frontend React + Vite, servido por Nginx
├── k8s/                          # Manifiestos de Kubernetes para despliegue en AWS EKS
├── .github/workflows/            # Pipelines de CI/CD (tests, build, push a ECR, deploy a EKS)
└── docker-compose.yml            # Entorno local de desarrollo
```

---

## Despliegue en AWS (EKS)

El despliegue en Kubernetes/AWS, los prerequisitos de infraestructura y el
orden de aplicación de los manifiestos están documentados en detalle en
[`k8s/README.md`](k8s/README.md).
