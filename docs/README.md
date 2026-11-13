# UCO Kudos — Back-End

Plataforma institucional de reconocimientos de la Universidad Católica de Oriente.
Proyecto Integrador Final · Ingeniería del Software II · 2026-2.

| | |
|---|---|
| API pública | `https://CAMBIAR.onrender.com` (health: `/actuator/health`) |
| Front-End | `https://CAMBIAR.onrender.com` · repositorio `uco-kudos-web` |
| Stack | Java 21 · Spring Boot 4 · PostgreSQL 17 · Flyway · Spring Security (JWT) · Micrometer/Prometheus · Docker |
| DevSecOps | GitHub Actions · SonarQube Cloud · Trivy · GHCR · Render |

## Flujo obligatorio implementado: Publicar Kudo

1. El Kudo (autor, destinatario, categoría, fecha, contenido sin límite, adjuntos) se persiste.
2. El destinatario recibe un correo (Brevo).
3. El destinatario recibe un WhatsApp (Twilio).
4. Si está conectado, recibe una notificación push en tiempo real (Server-Sent Events).
5. Todo el proceso queda auditado (`audit_events` + logs con traceId).

## Documentación

| # | Documento |
|---|---|
| 1 | [Visión, alcance (línea base vs. objetivo), drivers y atributos de calidad](docs/01-vision-alcance-y-drivers.md) |
| 2 | [Diseño: diagramas de nivel 1, nivel 2, componentes, paquetes, secuencia y modelo de datos](docs/02-diseno-arquitectonico.md) |
| 3 | [ABB y SBB](docs/03-abb-sbb.md) |
| 4 | [ADRs](docs/04-adrs.md) |
| 5 | [Clean Architecture, SOLID, Clean Code y patrones](docs/05-clean-architecture-solid-patrones.md) |
| 6 | [Manifiesto Reactivo y matriz Twelve-Factor](docs/06-reactive-manifesto-y-twelve-factor.md) |
| 7 | [Desarrollo seguro](docs/07-desarrollo-seguro.md) |
| 8 | [Pirámide de pruebas, observabilidad y DevSecOps](docs/08-pruebas-observabilidad-devsecops.md) |
| 9 | [Manual de puesta a punto](docs/09-manual-puesta-a-punto.md) |
| 10 | [Evidencias](docs/10-evidencias.md) |

## Inicio rápido

```bash
docker compose up -d db
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# http://localhost:8080/actuator/health
```

## API

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/v1/auth/login` | Público |
| GET | `/api/v1/users/me` · PUT `/api/v1/users/me/contact` | Autenticado |
| GET | `/api/v1/users` (directorio) | Autenticado |
| GET / POST | `/api/v1/categories` | GET autenticado · POST Admin, Rector, Coord. Académico |
| POST | `/api/v1/kudos` (multipart: `recipientId`, `categoryId`, `content`, `files[]`) | Autenticado |
| GET | `/api/v1/kudos?page=&size=` · `/api/v1/kudos/{id}` · `/api/v1/kudos/{id}/attachments/{attId}` | Autenticado |
| GET | `/api/v1/notifications/stream` (SSE) | Autenticado (`?access_token=`) |
| GET | `/api/v1/audit?resourceId=` | Admin, Rector, Coord. Académico |
| GET | `/actuator/health`, `/actuator/prometheus` | Público |

## Estructura

```text
src/main/java/co/edu/uco/ucokudos
├── domain/            Reglas de negocio puras (sin frameworks)
├── services/          Casos de uso + puertos (sin Spring)
└── infrastructure/
    ├── input/         REST, listener de eventos
    ├── output/        JPA, correo, WhatsApp, push SSE, JWT, transacciones
    └── config/        Composition Root, seguridad, async, observabilidad, datos demo
```
