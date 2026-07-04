# ============================================================
# Stage 1: Build — compila el .jar con Maven
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos pom.xml primero para aprovechar el cache de capas de Docker
# (las dependencias se descargan solo cuando pom.xml cambia)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y empaquetamos
COPY src ./src
RUN mvn clean package -DskipTests

# ============================================================
# Stage 2: Runtime — imagen mínima con solo el JRE
# ============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuario no-root por seguridad (evita que el proceso corra como root dentro del contenedor)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiamos únicamente el .jar generado y lo renombramos a app.jar
# para no depender del nombre de versión exacto generado por Maven
COPY --chown=appuser:appgroup --from=build /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

# Las variables DB_ENDPOINT, DB_PORT, DB_NAME, DB_USERNAME y DB_PASSWORD
# son inyectadas por Kubernetes en runtime desde el Secret db-credentials-ventas.
# NUNCA hardcodearlas aquí.
ENTRYPOINT ["java", "-jar", "app.jar"]
