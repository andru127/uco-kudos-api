# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecución (imagen mínima, solo JRE) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Usuario sin privilegios (Trivy/CIS: no ejecutar como root)
RUN addgroup -S kudos && adduser -S kudos -G kudos
COPY --from=build /app/target/uco-kudos-api.jar app.jar
USER kudos
EXPOSE 8080
# MaxRAMPercentage: respeta el límite de memoria del contenedor (Render free = 512 MB)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
