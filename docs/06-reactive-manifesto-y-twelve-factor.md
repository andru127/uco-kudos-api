# 6. Manifiesto Reactivo y Twelve-Factor App

## 6.1 Manifiesto de Sistemas Reactivos

| Propiedad | Qué exige | Cómo lo aborda UCO Kudos (línea base) | Brecha y evolución |
|---|---|---|---|
| **Responsivo** | Responder a tiempo y de forma consistente | Publicar responde 201 sin esperar a terceros (notificación asíncrona); muro desde caché; timeouts de 5 s/10 s a proveedores; paginación acotada; front con feedback inmediato (toasts, estados de carga). | Medir p95 con Grafana y fijar SLO. |
| **Resiliente** | Mantenerse responsivo ante fallos | Aislamiento por canal (un fallo de Twilio no impide correo ni push); reintentos con backoff; contención de excepciones en el decorador; bulkhead (pool dedicado); toda falla queda auditada (`NOTIFICATION_FAILED`); SSE con reconexión automática; *graceful shutdown*; health checks para el orquestador. | Outbox + broker para no perder notificaciones si cae la instancia; circuit breaker (Resilience4j). |
| **Elástico** | Responder a cambios de carga | API stateless (JWT, sin sesión en servidor) → puede escalar horizontalmente; caché local; pool de conexiones acotado; imagen que respeta límites de memoria del contenedor. | Autoescalado (plan pago/K8s); Redis para caché y SSE compartidos entre instancias. |
| **Orientado a mensajes** | Comunicación asíncrona por mensajes, bajo acoplamiento | `KudoPublishedEvent` desacopla el emisor (caso de uso) del consumidor (notificaciones); push al navegador por SSE (mensajes servidor→cliente); el caso de uso desconoce cuántos canales existen. | Broker externo (RabbitMQ/Kafka) con entrega al menos una vez y DLQ. |

## 6.2 Matriz Twelve-Factor App

Escala: ✅ Cumple · 🟡 Cumple parcialmente · ❌ No cumple

| # | Factor | Evaluación | Análisis y justificación |
|---|---|---|---|
| I | **Código base** | ✅ | Un repositorio por desplegable (`uco-kudos-api`, `uco-kudos-web`) versionado en Git; el mismo código se despliega en todos los entornos. |
| II | **Dependencias** | ✅ | Declaradas y aisladas: `pom.xml` + Maven Wrapper; `package.json` + `package-lock.json` (`npm ci`). La imagen Docker no depende de nada instalado en el host. |
| III | **Configuración** | ✅ | Todo lo que cambia por entorno llega por variables de entorno (`DATABASE_URL`, `JWT_SECRET`, `BREVO_API_KEY`, `TWILIO_*`, `CORS_ALLOWED_ORIGINS`…), validado al arrancar (`SecurityProperties` falla si el secreto es corto). Sin secretos en el repositorio (Trivy escanea secretos). |
| IV | **Servicios de respaldo** | ✅ | PostgreSQL, Brevo y Twilio se tratan como recursos adjuntos intercambiables por URL/credenciales; cambiar de proveedor de correo es cambiar `EMAIL_PROVIDER`. |
| V | **Construir, publicar, ejecutar** | ✅ | Etapas separadas: build (`mvn package` / `vite build`), release (imagen etiquetada con el SHA en GHCR + config del entorno), run (Render). |
| VI | **Procesos** | 🟡 | API stateless (JWT, nada en sesión); los datos persistentes viven en PostgreSQL. Excepción consciente: el registro de conexiones SSE y la caché son memoria local (ADR-004/005). |
| VII | **Asignación de puertos** | ✅ | La API se autoexpone en `${PORT}` (Tomcat embebido); Render inyecta el puerto. |
| VIII | **Concurrencia** | 🟡 | Escala por procesos (más instancias del contenedor) y usa un pool de hilos dedicado para trabajo asíncrono. Escalar a N instancias requiere resolver el punto VI. |
| IX | **Desechabilidad** | ✅ | Arranque rápido, `server.shutdown=graceful`, migraciones idempotentes con Flyway, *seeder* idempotente; el contenedor se puede matar en cualquier momento. |
| X | **Paridad dev/prod** | ✅ | Mismo motor (PostgreSQL 17) en local con `docker compose` y en Render; misma imagen Docker. Las pruebas usan H2 en modo PostgreSQL por velocidad (riesgo menor documentado). |
| XI | **Logs** | ✅ | Se escriben a stdout como flujo de eventos (ECS JSON en prod) con traceId; la plataforma los recolecta. |
| XII | **Procesos de administración** | ✅ | Las migraciones (Flyway) y la carga de datos iniciales corren como procesos únicos dentro del mismo código y entorno al arrancar. |

**Resultado:** 10 ✅, 2 🟡, 0 ❌. Las dos brechas tienen una evolución definida (Redis compartido).
