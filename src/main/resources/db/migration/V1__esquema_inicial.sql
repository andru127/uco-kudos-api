-- =====================================================================
-- UCO Kudos - Esquema inicial de la línea base
-- Compatible con PostgreSQL (producción) y H2 en modo PostgreSQL (pruebas)
-- =====================================================================

-- Estructura académica (soporta la jerarquía institucional)
CREATE TABLE faculties (
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE programs (
    id          UUID PRIMARY KEY,
    faculty_id  UUID NOT NULL REFERENCES faculties (id),
    name        VARCHAR(150) NOT NULL UNIQUE
);

-- Un único rol activo por persona (exclusividad): columna role, no tabla N:M.
CREATE TABLE users (
    id            UUID PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone         VARCHAR(20)  NOT NULL,
    role          VARCHAR(40)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Un estudiante puede estudiar varios programas; un docente enseñar en varios;
-- un coordinador de programa acompaña un programa.
CREATE TABLE user_programs (
    user_id     UUID        NOT NULL REFERENCES users (id),
    program_id  UUID        NOT NULL REFERENCES programs (id),
    relation    VARCHAR(20) NOT NULL, -- ESTUDIA | ENSENA | COORDINA
    PRIMARY KEY (user_id, program_id, relation)
);

CREATE TABLE categories (
    id          UUID PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL UNIQUE,
    description VARCHAR(300),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Contenido TEXT: el requisito indica que no hay límite de longitud.
CREATE TABLE kudos (
    id            UUID PRIMARY KEY,
    author_id     UUID NOT NULL REFERENCES users (id),
    recipient_id  UUID NOT NULL REFERENCES users (id),
    category_id   UUID NOT NULL REFERENCES categories (id),
    content       TEXT NOT NULL,
    published_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Índices pensados para la carga predominante de lectura (DRV-002)
CREATE INDEX idx_kudos_published_at ON kudos (published_at DESC);
CREATE INDEX idx_kudos_recipient ON kudos (recipient_id);

CREATE TABLE kudo_attachments (
    id            UUID PRIMARY KEY,
    kudo_id       UUID         NOT NULL REFERENCES kudos (id),
    file_name     VARCHAR(160) NOT NULL,
    content_type  VARCHAR(80)  NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    sha256        VARCHAR(64)  NOT NULL,
    content       BYTEA        NOT NULL
);

CREATE INDEX idx_attachments_kudo ON kudo_attachments (kudo_id);

-- Auditoría: append-only, nunca se actualiza ni se borra desde la aplicación.
CREATE TABLE audit_events (
    id             UUID PRIMARY KEY,
    occurred_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    actor_id       UUID,
    action         VARCHAR(40)   NOT NULL,
    resource_type  VARCHAR(30)   NOT NULL,
    resource_id    UUID,
    detail         VARCHAR(1000),
    trace_id       VARCHAR(64)
);

CREATE INDEX idx_audit_resource ON audit_events (resource_id);
CREATE INDEX idx_audit_occurred_at ON audit_events (occurred_at DESC);
