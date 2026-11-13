# 2. Diseño arquitectónico (alto nivel y detallado)

Notación: modelo C4 (nivel 1 contexto, nivel 2 contenedores, nivel 3 componentes), representado en texto plano.

## 2.1 Diagrama de nivel 1 — Contexto

```text
  +---------------------------+                         +---------------------------+
  | Comunidad UCO             |                         | Administrador / Rector /  |
  | (estudiantes, docentes,   |                         | Coordinador Académico     |
  | coordinadores, directivos)|                         +-------------+-------------+
  +-------------+-------------+                                       |
                | Publica y consulta Kudos                            | Administra categorías,
                | (celular 90 %)                                      | consulta auditoría
                v                                                     v
        +--------------------------------------------------------------------+
        |                            UCO Kudos                               |
        +-------------+--------------------------------------+---------------+
                      | Envía correo (HTTPS)                 | Envía WhatsApp (HTTPS)
                      v                                      v
              +---------------+                      +---------------+
              | Brevo         |                      | Twilio        |
              | (correo)      |                      | (WhatsApp)    |
              +-------+-------+                      +-------+-------+
                      | correo al destinatario               | WhatsApp al destinatario
                      v                                      v
                                  Comunidad UCO

  +---------------------------+   despliega    +-----------+
  | GitHub (código, CI/CD,    | -------------> | UCO Kudos |
  | registro de imágenes)     |                +-----------+
  |                           |   analiza      +-------------------+
  |                           | -------------> | SonarQube Cloud   |
  +---------------------------+                +-------------------+
```

## 2.2 Diagrama de nivel 2 — Contenedores

```text
  Dispositivo del usuario                       Render (PaaS)
  +------------------------------+              +---------------------------------------------+
  |  SPA UCO Kudos               |  HTML/JS/CSS |  Static Site (CDN, HTTPS)                   |
  |  React 19 + TypeScript +     |<-------------|                                             |
  |  Vite, Mobile First          |              |                                             |
  |                              |  REST/JSON + |  uco-kudos-api                              |
  |                              |  JWT,        |  Spring Boot 4, Java 21, contenedor Docker  |
  |                              |  multipart   |                                             |
  |                              |------------->|                                             |
  |                              |<-------------|   SSE: push en tiempo real                  |
  +------------------------------+              |          |                                  |
                                                |          | JDBC (Flyway)                    |
                                                |          v                                  |
                                                |  PostgreSQL (kudos, adjuntos, usuarios,     |
                                                |  auditoría)                                 |
                                                +----------+----------------------------------+
                                                           | HTTPS
                                          +----------------+----------------+
                                          v                                 v
                                   +-------------+                  +-------------+
                                   |  Brevo API  |                  | Twilio API  |
                                   +-------------+                  +-------------+
```

| Contenedor | Tecnología | Responsabilidad |
|---|---|---|
| SPA | React + Vite (estático) | Interfaz móvil, sesión, conexión SSE |
| API | Spring Boot (monolito modular hexagonal) | Reglas de negocio, seguridad, notificaciones, auditoría, métricas |
| Base de datos | PostgreSQL 17 | Persistencia transaccional (incluye binarios de adjuntos en la línea base, ver ADR-006) |

## 2.3 Diagrama de componentes (nivel 3 — API)

```text
  infrastructure.input (adaptadores de entrada)
  +-----------------------------------------------------------------------------------+
  | KudoController  AuthController  CategoryController  UserController                |
  | NotificationStreamController  AuditController  KudoPublishedListener              |
  +---------------------------------------+-------------------------------------------+
                                          | invoca
                                          v
  services (casos de uso y puertos)
  +-----------------------------------------------------------------------------------+
  | PublishKudoInteractor ------> KudoRepository, AuditRepository,                    |
  |                               KudoEventPublisher, TransactionRunner (puertos)     |
  | NotifyKudoRecipientInteractor -> NotificationSender, AuditRepository (puertos)    |
  | LoginInteractor -------------> TokenIssuer, PasswordHasher (puertos)              |
  | Feed, Categorías, Usuarios, Auditoría (otros casos de uso)                        |
  +---------------------------------------+-------------------------------------------+
                                          | usa
                                          v
  domain
  +-----------------------------------------------------------------------------------+
  | KudoDomain   UserDomain   Role   AttachmentDomain   AuditEvent                    |
  +-----------------------------------------------------------------------------------+

  infrastructure.output (adaptadores de salida que IMPLEMENTAN los puertos)
  +-----------------------------------------------------------------------------------+
  | Adaptadores JPA ...................... implementa KudoRepository, AuditRepository |
  | ResilientNotificationSender (decorador) implementa NotificationSender y envuelve: |
  |     EmailNotificationSender, TwilioWhatsAppNotificationSender,                    |
  |     SsePushNotificationSender                                                     |
  | SpringKudoEventPublisher ............. implementa KudoEventPublisher              |
  |                                        (entrega el evento a KudoPublishedListener)|
  | JwtTokenIssuer, BCryptPasswordHasher . implementa TokenIssuer, PasswordHasher     |
  +-----------------------------------------------------------------------------------+
```

Las flechas de dependencia siempre apuntan hacia el dominio. Los adaptadores de salida implementan interfaces (puertos) definidas en `services`.

## 2.4 Diagrama de paquetes

```text
co.edu.uco.ucokudos
|
+-- domain
|     user, category, kudo, notification, audit, exception
|
+-- services                                   (depende solo de domain)
|     usecases       kudo, notification, auth, category, user, audit
|     input          commands, queries, PageResult
|     output.ports   repositories, notifications, events, security, transaction
|
+-- infrastructure                             (depende de services y domain)
      input.rest                        controllers, dto, exception handler
      input.events                      listener asíncrono
      output.repositories.structured    JPA, mappers, adapters
      output.notifications              email, whatsapp, push, decorador resiliente
      output.security, output.events, output.transaction
      config                            security, async, observability, seed, UseCaseConfig

Dirección de dependencias:  infrastructure ---> services ---> domain
```

Reglas (verificadas automáticamente por `CleanArchitectureRulesTest`):

1. `domain` no importa nada de `services`, `infrastructure`, Spring ni JPA.
2. `services` no importa Spring, JPA ni `infrastructure`.
3. Solo `infrastructure.config.UseCaseConfig` (Composition Root) conoce casos de uso e implementaciones a la vez.

## 2.5 Diagramas de secuencia principales

### 2.5.1 Publicar Kudo (flujo obligatorio de la línea base)

```text
Participantes: Autor, SPA, Filtros (TraceIdFilter + Spring Security), KudoController,
PublishKudoInteractor, KudoDomain, PostgreSQL, Bus de eventos, KudoPublishedListener,
NotifyKudoRecipientInteractor, Brevo, Twilio, SSE del destinatario.

 1. Autor                  -> SPA                    : completa el formulario y los adjuntos
 2. SPA                    -> Filtros                : POST /api/v1/kudos (multipart, Bearer JWT)
 3. Filtros                -> Filtros                : asigna traceId en MDC, valida JWT y rol
 4. Filtros                -> KudoController         : petición autenticada
 5. KudoController         -> KudoController         : detecta el tipo real de los adjuntos (magic bytes)
 6. KudoController         -> PublishKudoInteractor  : execute(PublishKudoCommand)
 7. PublishKudoInteractor  -> PostgreSQL             : busca autor, destinatario y categoría
 8. PublishKudoInteractor  -> KudoDomain             : KudoDomain.publish(...) valida las reglas
    --- inicio de la transacción ---
 9. PublishKudoInteractor  -> PostgreSQL             : INSERT kudo y adjuntos              (Paso 1)
10. PublishKudoInteractor  -> PostgreSQL             : INSERT auditoría KUDO_PUBLISHED     (Paso 5)
11. PublishKudoInteractor  -> Bus de eventos         : publica KudoPublishedEvent
    --- commit ---
12. PublishKudoInteractor  -> KudoController         : devuelve el Kudo
13. KudoController         -> SPA                    : 201 Created + cabecera X-Trace-Id
14. SPA                    -> Autor                  : "Kudo publicado"
    --- a partir de aquí todo es asíncrono (hilo notify-*) ---
15. Bus de eventos         -> KudoPublishedListener  : AFTER_COMMIT
16. KudoPublishedListener  -> NotifyKudoRecipient    : execute(evento)
17. NotifyKudoRecipient    -> Brevo                  : correo con reintentos                (Paso 2)
18. NotifyKudoRecipient    -> PostgreSQL             : auditoría NOTIFICATION_*
19. NotifyKudoRecipient    -> Twilio                 : WhatsApp con reintentos              (Paso 3)
20. NotifyKudoRecipient    -> PostgreSQL             : auditoría NOTIFICATION_*
21. NotifyKudoRecipient    -> SSE del destinatario   : evento "kudo" si está conectado      (Paso 4)
22. NotifyKudoRecipient    -> PostgreSQL             : auditoría NOTIFICATION_SENT o SKIPPED
```

### 2.5.2 Conexión en tiempo real (push)

```text
Participantes: Destinatario, SPA, NotificationStreamController, SseConnectionRegistry.

 1. Destinatario                 -> SPA                          : inicia sesión
 2. SPA                          -> NotificationStreamController : GET /api/v1/notifications/stream?access_token=JWT
 3. NotificationStreamController -> SseConnectionRegistry        : connect(userId)
 4. SseConnectionRegistry        -> SPA                          : event: connected
 5. SseConnectionRegistry        -> SPA                          : event: heartbeat (cada 25 s, en bucle)
    --- cuando se publica un Kudo para el destinatario ---
 6. SseConnectionRegistry        -> SPA                          : event: kudo {kudoId, title, body}
 7. SPA                          -> Destinatario                 : aviso en la app, notificación del sistema y muro actualizado
```

### 2.5.3 Inicio de sesión

```text
Participantes: Usuario, AuthController, LoginInteractor, UserRepository, BCrypt, JwtTokenIssuer, Auditoría.

 1. Usuario         -> AuthController  : POST /api/v1/auth/login {email, password}
 2. AuthController  -> LoginInteractor : execute
 3. LoginInteractor -> UserRepository  : findCredentialsByEmail
 4. LoginInteractor -> BCrypt          : matches(password, hash)
    --- si las credenciales son válidas ---
 5. LoginInteractor -> JwtTokenIssuer  : issue(user): HS256, expira en 8 h, incluye el rol
 6. LoginInteractor -> Auditoría       : LOGIN_SUCCEEDED
 7. AuthController  -> Usuario         : 200 {accessToken, user}
    --- si son inválidas ---
 5. LoginInteractor -> Auditoría       : LOGIN_FAILED
 6. AuthController  -> Usuario         : 401 "Credenciales inválidas" (siempre el mismo mensaje)
```

## 2.6 Modelo de datos

```text
FACULTIES 1 ---- N PROGRAMS
USERS     1 ---- N USER_PROGRAMS N ---- 1 PROGRAMS        (relación: ESTUDIA, ENSENA, COORDINA)
USERS     1 ---- N KUDOS   (como autor)
USERS     1 ---- N KUDOS   (como destinatario)
CATEGORIES 1 --- N KUDOS
KUDOS     1 ---- N KUDO_ATTACHMENTS
KUDOS     1 ---- N REPLIES                 (arquitectura objetivo)
REPLIES   1 ---- N REPLIES                 (réplica de réplica, arquitectura objetivo)
USERS     1 ---- N RATINGS                 (arquitectura objetivo)
USERS     1 ---- N REPORTS                 (arquitectura objetivo)

USERS            id (PK), email (único), phone, role, active, password_hash
KUDOS            id (PK), author_id (FK), recipient_id (FK), category_id (FK), content (text), published_at
KUDO_ATTACHMENTS id (PK), kudo_id (FK), file_name, content_type, size_bytes, sha256, content (bytea)
AUDIT_EVENTS     id (PK), occurred_at, actor_id, action, resource_id, detail, trace_id
REPLIES          id (PK), kudo_id (FK), parent_reply_id (FK), author_id (FK), content (text), status
RATINGS          id (PK), target_type, target_id, user_id, stars, active
REPORTS          id (PK), target_type, target_id, reporter_id, reason, status, vetoed_by
```

Notas de la arquitectura objetivo:

- Réplicas sin límite de profundidad: lista de adyacencia (`parent_reply_id`) + `kudo_id` desnormalizado para leer todo el hilo de un Kudo en una sola consulta; la UI construye el árbol.
- Una calificación activa por usuario y contenido: índice único parcial `UNIQUE (user_id, target_type, target_id) WHERE active`.
- Unicidad de roles: índice único parcial `UNIQUE (role) WHERE active AND role IN ('RECTOR','COORDINADOR_ACADEMICO')`; para Decano y Coordinador de Programa, unicidad sobre `user_programs` con `relation='COORDINA'`.
- Veto: solo `Role.canVetoContent()` (Rector y Coordinador Académico); el contenido vetado se oculta (borrado lógico) y se audita.
