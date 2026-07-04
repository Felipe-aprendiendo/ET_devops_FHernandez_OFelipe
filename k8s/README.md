# Manifiestos de Kubernetes — Sistema Ventas-Despacho

Estos archivos se aplican **desde la instancia EC2 pivote** (o cualquier máquina con
`kubectl` configurado) antes de que los pipelines de GitHub Actions puedan funcionar.

---

## ADVERTENCIA — Orden de operaciones crítico

Los pipelines (`.github/workflows/`) hacen `kubectl set image` sobre Deployments
que deben existir previamente. Si no existen, el pipeline fallará con:

```
Error from server (NotFound): deployments.apps "backend-ventas" not found
```

**Aplica estos manifiestos al menos una vez ANTES de hacer el primer push a la rama `deploy`.**

---

## Prerequisitos en AWS (una sola vez)

### 1. kubectl y kubeconfig

```bash
# En la instancia EC2 pivote, instalar kubectl
curl -LO "https://dl.k8s.io/release/v1.30.0/bin/linux/amd64/kubectl"
chmod +x kubectl && sudo mv kubectl /usr/local/bin/

# Configurar acceso al clúster EKS
aws eks update-kubeconfig --name <NOMBRE_CLUSTER_EKS> --region us-east-1
```

### 2. Permisos IAM sobre el clúster (crítico)

`aws eks update-kubeconfig` genera el kubeconfig, pero EKS exige además que el IAM
principal esté autorizado dentro del propio clúster. Sin este paso, cualquier `kubectl`
falla con error de autorización aunque las credenciales AWS sean válidas.

**Opción A — EKS Access Entries** (clústeres modernos, recomendado):
```bash
# Autorizar el usuario/rol IAM que usará kubectl (EC2 pivote o pipelines)
aws eks create-access-entry \
  --cluster-name <NOMBRE_CLUSTER_EKS> \
  --principal-arn arn:aws:iam::<CUENTA_AWS>:role/<ROL_O_USUARIO_IAM> \
  --region us-east-1

aws eks associate-access-policy \
  --cluster-name <NOMBRE_CLUSTER_EKS> \
  --principal-arn arn:aws:iam::<CUENTA_AWS>:role/<ROL_O_USUARIO_IAM> \
  --policy-arn arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy \
  --access-scope type=cluster \
  --region us-east-1
```

**Opción B — ConfigMap aws-auth** (clústeres más antiguos):
```bash
kubectl edit configmap aws-auth -n kube-system
# Agregar el ARN del usuario/rol bajo mapUsers o mapRoles
```

> En AWS Academy Learner Lab las credenciales cambian cada sesión.
> Puede ser necesario repetir el paso de Access Entries si el ARN del principal cambia.

### 3. Metrics Server (necesario para los HPAs)

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

---

## Aplicar los manifiestos

```bash
# 1. Editar los Secrets con los valores reales
#    (NUNCA commitear con datos reales — solo editar localmente antes del apply)
#    IMPORTANTE: DB_PASSWORD en 01 y 02 debe coincidir EXACTAMENTE con
#    MYSQL_ROOT_PASSWORD en 03 — son la misma credencial de MySQL compartida.
nano k8s/01-secret-db-ventas.yaml
nano k8s/02-secret-db-despachos.yaml
nano k8s/03-secret-mysql-root.yaml

# 2. Aplicar todo el directorio en orden numérico
kubectl apply -f k8s/

# O archivo por archivo si se prefiere:
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-secret-db-ventas.yaml
kubectl apply -f k8s/02-secret-db-despachos.yaml
kubectl apply -f k8s/03-secret-mysql-root.yaml
kubectl apply -f k8s/05-mysql-deployment.yaml
kubectl apply -f k8s/06-mysql-service.yaml
kubectl apply -f k8s/10-backend-ventas-deployment.yaml
kubectl apply -f k8s/11-backend-ventas-service.yaml
kubectl apply -f k8s/12-backend-ventas-hpa.yaml
kubectl apply -f k8s/20-backend-despachos-deployment.yaml
kubectl apply -f k8s/21-backend-despachos-service.yaml
kubectl apply -f k8s/22-backend-despachos-hpa.yaml
kubectl apply -f k8s/30-frontend-deployment.yaml
kubectl apply -f k8s/31-frontend-service.yaml
kubectl apply -f k8s/32-frontend-hpa.yaml
```

> La base de datos es un único contenedor MySQL (Deployment `mysql`, Service
> `mysql`) corriendo dentro del propio clúster, con almacenamiento `emptyDir`
> (sin PVC/EBS). Ambos backends crean su base automáticamente gracias a
> `createDatabaseIfNotExist=true`, usando el mismo usuario `root` compartido.

---

## Verificación post-deploy

```bash
kubectl get pods -n sistema-despachos
kubectl get svc  -n sistema-despachos
kubectl get hpa  -n sistema-despachos

# Logs de los backends
kubectl logs deployment/backend-ventas    -n sistema-despachos
kubectl logs deployment/backend-despachos -n sistema-despachos

# URL pública del frontend (puede tardar 1-3 min en propagarse)
kubectl get svc frontend -n sistema-despachos \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```
