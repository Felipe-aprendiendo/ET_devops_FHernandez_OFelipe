# Frontend — Sistema Ventas-Despacho (Innovatech Chile)

## ¿Qué hace este servicio?

SPA (Single Page Application) en React 18 + Vite que permite al área de despacho
gestionar órdenes de compra y despachos. Se comunica con los dos backends a través
de un proxy inverso de Nginx — ninguna llamada sale directamente al exterior desde
el navegador.

**Stack:** React 18, Vite, Axios, Tailwind CSS, React Router v6, React Hook Form, SweetAlert2

---

## Cómo correrlo localmente

```bash
# Dentro de la carpeta front_despacho/
npm install
npm run dev
```

Para desarrollo local, el proxy de Vite (`vite.config.js`) puede redirigir las
llamadas a los backends. Edítalo según corresponda a tu entorno local.

---

## Variables de entorno

Las variables `VITE_API_VENTAS` y `VITE_API_DESPACHOS` ya **no se usan** (ver `.env.example`).
En producción (EKS), el proxy de Nginx resuelve los backends internamente;
en desarrollo local, configura el proxy en `vite.config.js`.

---

## Docker — build y ejecución local

```bash
# Build de la imagen (desde la carpeta front_despacho/)
docker build -t sistema-despachos-frontend:local .

# Ejecutar localmente (mapea puerto 80 del contenedor al 8090 del host)
docker run -p 8090:80 sistema-despachos-frontend:local
```

La app queda disponible en `http://localhost:8090`.

**Nota:** en el contenedor, Nginx intenta resolver `backend-ventas` y `backend-despachos`
como nombres de host. Para probar el contenedor aislado, los backends deben estar en la
misma red Docker o debes ajustar `nginx.conf` temporalmente.

---

## Pipeline CI/CD

**Archivo:** `.github/workflows/deploy-frontend.yml`

| Paso | Qué hace |
|------|----------|
| Trigger | Push a la rama `deploy` con cambios en `front_despacho/**` |
| Build | `docker build` de la imagen multi-stage (Node → Nginx) |
| Push | Sube la imagen a ECR con tag `${{ github.sha }}` y `latest` |
| Deploy | `kubectl set image` + `kubectl rollout status` en EKS |

---

## Despliegue en EKS

A diferencia de EP2 (EC2 + Docker Compose), en EP3 el frontend corre en un pod de
Kubernetes. El Service `frontend` de tipo `LoadBalancer` crea automáticamente un
Network Load Balancer en AWS para exponer la app al exterior.

Manifiestos: [`k8s/30-frontend-deployment.yaml`](../k8s/30-frontend-deployment.yaml), [`k8s/31-frontend-service.yaml`](../k8s/31-frontend-service.yaml), [`k8s/32-frontend-hpa.yaml`](../k8s/32-frontend-hpa.yaml)
