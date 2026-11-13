# 2. Diseño arquitectónico (alto nivel y detallado)

Notación: modelo **C4** (nivel 1 contexto, nivel 2 contenedores, nivel 3 componentes) en Mermaid.

## 2.1 Diagrama de nivel 1 — Contexto

```mermaid
flowchart LR
    U["👤 Comunidad UCO<br/>(estudiantes, docentes,<br/>coordinadores, directivos)"]
    A["👤 Administrador /<br/>Rector / Coord. Académico"]
    S(("UCO Kudos"))
    B["✉️ Brevo<br/>(correo transaccional)"]
    T["💬 Twilio<br/>(WhatsApp)"]
    G["⚙️ GitHub<br/>(código, CI/CD, GHCR)"]
    SC["🔎 SonarQube Cloud"]

    U -- "Publica y consulta Kudos<br/>(celular 90 %)" --> S
    A -- "Administra categorías,<br/>consulta auditoría" --> S
    S -- "Envía correo (HTTPS)" --> B
    S -- "Envía WhatsApp (HTTPS)" --> T
    B -. correo .-> U
    T -. WhatsApp .-> U
    G -- "Despliega" --> S
    G -- "Analiza código" --> SC
```

## 2.2 Diagrama de nivel 2 — Contenedores

```mermaid
flowchart TB
    subgraph Usuario["Dispositivo del usuario"]
        W["SPA UCO Kudos<br/>React 19 + TypeScript + Vite<br/>Mobile First"]
    end

    subgraph Render["Render (PaaS)"]
        ST["Static Site<br/>(CDN, HTTPS)"]
        API["uco-kudos-api<br/>Spring Boot 4 · Java 21<br/>contenedor Docker"]
        DB[("PostgreSQL<br/>kudos, adjuntos,<br/>usuarios, auditoría")]
    end

    BR["Brevo API"]
    TW["Twilio API"]

    ST -- "HTML/JS/CSS" --> W
    W -- "REST/JSON + JWT<br/>multipart (adjuntos)" --> API
    API -- "SSE: push en tiempo real" --> W
    API -- "JDBC (Flyway)" --> DB
    API -- "HTTPS" --> BR
    API -- "HTTPS" --> TW
```

| Contenedor | Tecnología | Responsabilidad |
|---|---|---|
| SPA | React + Vite (estático) | Interfaz móvil, sesión, conexión SSE |
| API | Spring Boot (monolito modular hexagonal) | Reglas de negocio, seguridad, notificaciones, auditoría, métricas |
| Base de datos | PostgreSQL 17 | Persistencia transaccional (incluye binarios de adjuntos en la línea base, ver ADR-006) |

## 2.3 Diagrama de componentes (nivel 3 — API)

```mermaid
flowchart LR
    subgraph IN["infrastructure.input (adaptadores de entrada)"]
        KC[KudoController]
        AC[AuthController]
        CC[CategoryController]
        UC[UserController]
        NSC[NotificationStreamController]
        AUC[AuditController]
        KL[KudoPublishedListener]
    end

    subgraph APP["services (casos de uso + puertos)"]
        PK[PublishKudoInteractor]
        NK[NotifyKudoRecipientInteractor]
        LI[LoginInteractor]
        OTH[Feed / Categorías / Usuarios / Auditoría]
        P1{{KudoRepository}}
        P2{{AuditRepository}}
        P3{{NotificationSender}}
        P4{{KudoEventPublisher}}
        P5{{TokenIssuer / PasswordHasher}}
        P6{{TransactionRunner}}
    end

    subgraph DOM["domain"]
        KD[KudoDomain]
        UD[UserDomain · Role]
        AD[AttachmentDomain]
        AE[AuditEvent]
    end

    subgraph OUT["infrastructure.output (adaptadores de salida)"]
        JPA[Adaptadores JPA]
        EM[EmailNotificationSender]
        WA[TwilioWhatsAppNotificationSender]
        PS[SsePushNotificationSender]
        RS[ResilientNotificationSender<br/>decorador]
        EV[SpringKudoEventPublisher]
        JWT[JwtTokenIssuer · BCrypt]
    end

    KC --> PK
    AC --> LI
    CC --> OTH
    UC --> OTH
    AUC --> OTH
    KL --> NK
    PK --> KD
    PK --> P1 & P2 & P4 & P6
    NK --> P3 & P2
    LI --> P5
    JPA -. implementa .-> P1 & P2
    RS -. implementa .-> P3
    RS --> EM & WA & PS
    EV -. implementa .-> P4
    JWT -. implementa .-> P5
    EV -- "evento tras commit" --> KL
```

Las flechas de dependencia siempre apuntan **hacia el dominio**. Los adaptadores de salida implementan interfaces (puertos) definidas en `services`.

## 2.4 Diagrama de paquetes

```mermaid
flowchart TB
    root["co.edu.uco.ucokudos"]
    root --> domain
    root --> services
    root --> infrastructure

    domain --> d1["user · category · kudo · notification · audit · exception"]
    services --> s1["usecases (kudo, notification, auth, category, user, audit)"]
    services --> s2["input (commands, queries, PageResult)"]
    services --> s3["output.ports (repositories, notifications, events, security, transaction)"]
    infrastructure --> i1["input.rest (controllers, dto, exception handler)"]
    infrastructure --> i2["input.events (listener asíncrono)"]
    infrastructure --> i3["output.repositories.structured (JPA, mappers, adapters)"]
    infrastructure --> i4["output.notifications (email, whatsapp, push, decorador resiliente)"]
    infrastructure --> i5["output.security · output.events · output.transaction"]
    infrastructure --> i6["config (security, async, observability, seed, UseCaseConfig)"]

    infrastructure -. depende de .-> services
    services -. depende de .-> domain
```

Reglas (verificadas automáticamente por `CleanArchitectureRulesTest`):

1. `domain` no importa nada de `services`, `infrastructure`, Spring ni JPA.
2. `services` no importa Spring, JPA ni `infrastructure`.
3. Solo `infrastructure.config.UseCaseConfig` (Composition Root) conoce casos de uso e implementaciones a la vez.

## 2.5 Diagramas de secuencia principales

### 2.5.1 Publicar Kudo (flujo obligatorio de la línea base)

```mermaid
sequenceDiagram
    autonumber
    actor A as Autor (móvil)
    participant W as SPA
    participant F as TraceIdFilter + Spring Security
    participant C as KudoController
    participant P as PublishKudoInteractor
    participant D as KudoDomain
    participant DB as PostgreSQL
    participant E as Bus de eventos
    participant L as KudoPublishedListener (hilo notify-*)
    participant N as NotifyKudoRecipientInteractor
    participant M as Brevo (correo)
    participant T as Twilio (WhatsApp)
    participant S as SSE (destinatario)

    A->>W: Completa formulario + adjuntos
    W->>F: POST /api/v1/kudos (multipart, Bearer JWT)
    F->>F: traceId en MDC · valida JWT y rol
    F->>C: petición autenticada
    C->>C: detecta tipo real de adjuntos (magic bytes)
    C->>P: execute(PublishKudoCommand)
    P->>DB: busca autor, destinatario, categoría
    P->>D: KudoDomain.publish(...) valida reglas
    rect rgb(230,245,238)
    note over P,DB: Transacción única
    P->>DB: INSERT kudo + adjuntos (Paso 1)
    P->>DB: INSERT audit KUDO_PUBLISHED (Paso 5)
    P->>E: publica KudoPublishedEvent
    end
    P-->>C: Kudo
    C-->>W: 201 Created + X-Trace-Id
    W-->>A: "¡Kudo publicado!"
    E-)L: AFTER_COMMIT (asíncrono)
    L->>N: execute(evento)
    N->>M: correo (reintentos) (Paso 2)
    N->>DB: audit NOTIFICATION_*
    N->>T: WhatsApp (reintentos) (Paso 3)
    N->>DB: audit NOTIFICATION_*
    N->>S: evento "kudo" si está conectado (Paso 4)
    N->>DB: audit NOTIFICATION_SENT / SKIPPED
```

### 2.5.2 Conexión en tiempo real (push)

```mermaid
sequenceDiagram
    actor D as Destinatario
    participant W as SPA
    participant API as NotificationStreamController
    participant R as SseConnectionRegistry
    D->>W: Inicia sesión
    W->>API: GET /api/v1/notifications/stream?access_token=JWT
    API->>R: connect(userId)
    R-->>W: event: connected
    loop cada 25 s
        R-->>W: event: heartbeat
    end
    note over R: Al publicarse un Kudo para D
    R-->>W: event: kudo {kudoId, title, body}
    W-->>D: Toast + notificación del sistema + refresca el muro
```

### 2.5.3 Inicio de sesión

```mermaid
sequenceDiagram
    actor U as Usuario
    participant API as AuthController
    participant L as LoginInteractor
    participant R as UserRepository
    participant H as BCrypt
    participant J as JwtTokenIssuer
    participant AU as Auditoría
    U->>API: POST /api/v1/auth/login {email, password}
    API->>L: execute
    L->>R: findCredentialsByEmail
    L->>H: matches(password, hash)
    alt credenciales válidas
        L->>J: issue(user) — HS256, exp 8 h, roles
        L->>AU: LOGIN_SUCCEEDED
        API-->>U: 200 {accessToken, user}
    else inválidas
        L->>AU: LOGIN_FAILED
        API-->>U: 401 "Credenciales inválidas" (mismo mensaje siempre)
    end
```

## 2.6 Modelo de datos

```mermaid
erDiagram
    FACULTIES ||--o{ PROGRAMS : tiene
    USERS ||--o{ USER_PROGRAMS : "estudia/enseña/coordina"
    PROGRAMS ||--o{ USER_PROGRAMS : ""
    USERS ||--o{ KUDOS : "autor"
    USERS ||--o{ KUDOS : "destinatario"
    CATEGORIES ||--o{ KUDOS : clasifica
    KUDOS ||--o{ KUDO_ATTACHMENTS : contiene
    KUDOS ||--o{ REPLIES : "recibe (objetivo)"
    REPLIES ||--o{ REPLIES : "réplica de réplica (objetivo)"
    USERS ||--o{ RATINGS : "califica (objetivo)"
    USERS ||--o{ REPORTS : "reporta (objetivo)"

    USERS {
        uuid id PK
        varchar email UK
        varchar phone
        varchar role
        boolean active
        varchar password_hash
    }
    KUDOS {
        uuid id PK
        uuid author_id FK
        uuid recipient_id FK
        uuid category_id FK
        text content
        timestamptz published_at
    }
    KUDO_ATTACHMENTS {
        uuid id PK
        uuid kudo_id FK
        varchar file_name
        varchar content_type
        bigint size_bytes
        varchar sha256
        bytea content
    }
    AUDIT_EVENTS {
        uuid id PK
        timestamptz occurred_at
        uuid actor_id
        varchar action
        uuid resource_id
        varchar detail
        varchar trace_id
    }
    REPLIES {
        uuid id PK
        uuid kudo_id FK
        uuid parent_reply_id FK
        uuid author_id FK
        text content
        varchar status
    }
    RATINGS {
        uuid id PK
        varchar target_type
        uuid target_id
        uuid user_id
        smallint stars
        boolean active
    }
    REPORTS {
        uuid id PK
        varchar target_type
        uuid target_id
        uuid reporter_id
        varchar reason
        varchar status
        uuid vetoed_by
    }
```

Notas de la arquitectura objetivo:

- **Réplicas sin límite de profundidad**: lista de adyacencia (`parent_reply_id`) + `kudo_id` desnormalizado para leer todo el hilo de un Kudo en una sola consulta; la UI construye el árbol.
- **Una calificación activa por usuario y contenido**: índice único parcial `UNIQUE (user_id, target_type, target_id) WHERE active`.
- **Unicidad de roles**: índice único parcial `UNIQUE (role) WHERE active AND role IN ('RECTOR','COORDINADOR_ACADEMICO')`; para Decano y Coordinador de Programa, unicidad sobre `user_programs` con `relation='COORDINA'`.
- **Veto**: solo `Role.canVetoContent()` (Rector y Coordinador Académico); el contenido vetado se oculta (borrado lógico) y se audita.
