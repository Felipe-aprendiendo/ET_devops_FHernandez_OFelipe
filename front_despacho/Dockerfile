# ============================================================
# Stage 1: Build — compila la app React + Vite
# ============================================================
FROM node:20-alpine AS build

WORKDIR /app

# Copiamos solo los archivos de dependencias primero para aprovechar el cache de Docker
COPY package.json package-lock.json ./
RUN npm ci

# Copiamos el resto del código y construimos la app estática
COPY . .
RUN npm run build
# El resultado queda en /app/dist/ — solo HTML, CSS y JS estático.
# No se necesitan variables VITE_API_* en build-time; el proxy de Nginx
# maneja el ruteo a los backends en tiempo de ejecución.

# ============================================================
# Stage 2: Servidor Nginx — sirve los estáticos y actúa como proxy
# ============================================================
FROM nginx:alpine

# Copiamos los archivos estáticos generados por Vite
COPY --from=build /app/dist /usr/share/nginx/html

# Copiamos la configuración de Nginx con el proxy inverso configurado
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
