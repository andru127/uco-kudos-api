# 4. Architecture Decision Records (ADR)

Formato: Contexto → Decisión → Alternativas → Consecuencias. Estado de todos: Aceptado (noviembre 2026).

---

## ADR-001 — Monolito modular con arquitectura hexagonal (Clean Architecture)

Contexto. Un solo desarrollador, 4 días, requisito de demostrar Clean Architecture y de poder evolucionar.

Decisión. Un único servicio desplegable organizado en capas `domain` → `services` (casos de uso + puertos) → `infrastructure` (adaptadores). Se mantiene la misma estructura de paquetes usada en el curso (`services/usecases`, `services/output/ports`, `infrastructure/output/repositories/structured`).

Alternativas. Microservicios (kudos, notificaciones, identidad): mayor costo operativo y de despliegue, no justificable en la línea base.

Consecuencias. (+) Despliegue y depuración simples; límites internos claros que permiten extraer el módulo de notificaciones como microservicio sin reescribir el negocio. (−) Escala como un todo.

---

## ADR-002 — Casos de uso sin dependencias de Spring (Composition Root)

Contexto. La regla de dependencia exige que el núcleo no conozca frameworks.

Decisión. Los interactors son clases Java planas. Se crean en `UseCaseConfig` con `@Bean`. La transacción se abstrae con el puerto `TransactionRunner` (implementado con `TransactionTemplate`).

Consecuencias. (+) Pruebas unitarias sin levantar Spring (milisegundos); el núcleo compila con `javac` puro. (−) Algo más de código de cableado. Se verifica con `CleanArchitectureRulesTest`.

---

## ADR-003 — Notificaciones asíncronas por evento de dominio después del commit

Contexto. Publicar un Kudo debe guardar, auditar y notificar por 3 canales externos que pueden ser lentos o fallar (DRV-003, responsividad).

Decisión. El caso de uso guarda Kudo + auditoría en una transacción y publica `KudoPublishedEvent`. `KudoPublishedListener` lo consume con `@TransactionalEventListener(AFTER_COMMIT)` y `@Async` en un pool dedicado (`notify-*`).

Alternativas. (a) Llamadas síncronas: la respuesta dependería de Twilio y Brevo. (b) RabbitMQ/Kafka: entrega garantizada, pero más infraestructura y costo.

Consecuencias. (+) Respuesta inmediata; solo se notifica lo que realmente se guardó; un canal caído no afecta a los otros. (−) Si la instancia muere entre el commit y el envío, la notificación se pierde. Evolución: patrón Transactional Outbox (tabla `outbox` escrita en la misma transacción + relay a un broker).

---

## ADR-004 — Caché en memoria para el muro y las categorías

Contexto. Carga predominantemente de lectura (DRV-002).

Decisión. Caffeine vía Spring Cache: `kudoFeed` (clave página:tamaño) y `categories`, TTL 30 s, máximo 500 entradas, invalidación total al publicar/crear.

Alternativas. Redis (compartido entre instancias, pero un servicio más); sin caché.

Consecuencias. (+) La mayoría de lecturas no tocan la BD. (−) Con varias instancias cada una tiene su caché (consistencia eventual ≤ 30 s). Evolución: Redis + réplicas de lectura de PostgreSQL + CQRS para el muro.

---

## ADR-005 — Push en tiempo real con Server-Sent Events

Contexto. "Si el destinatario está conectado debe recibir una notificación PUSH en tiempo real". La comunicación es solo servidor → cliente.

Decisión. SSE (`SseEmitter`) con un registro de conexiones por usuario, latido cada 25 s y autenticación por JWT en query string (EventSource no admite cabeceras).

Alternativas. WebSocket/STOMP (bidireccional, innecesario); Firebase Cloud Messaging / Web Push (funciona con la app cerrada, pero requiere service worker, claves VAPID y más tiempo).

Consecuencias. (+) HTTP estándar, reconexión automática del navegador, sin dependencias. "Conectado" queda definido con precisión: tiene una conexión SSE abierta. (−) Registro en memoria de una instancia; el token en la URL puede quedar en logs de proxies (mitigado: TTL del token y HTTPS). Evolución: Redis Pub/Sub para multi-instancia; Web Push para usuarios desconectados.

---

## ADR-006 — Adjuntos almacenados en PostgreSQL en la línea base

Contexto. Render free tiene disco efímero (se borra en cada despliegue); se necesitan adjuntos durables sin agregar otro proveedor en 4 días.

Decisión. Guardar el binario en `kudo_attachments.content` (`bytea`) con metadatos y SHA-256, límite 5 × 5 MB por Kudo. Las lecturas del muro usan una proyección sin el binario.

Alternativas. S3/R2/Cloudinary (escala mejor; requiere cuenta, credenciales y URLs prefirmadas).

Consecuencias. (+) Transaccional con el Kudo, respaldado con la BD, cero proveedores extra. (−) La BD crece más rápido. Evolución: puerto `AttachmentStorage` con adaptador S3/R2, URLs prefirmadas de corta vida y antivirus (ClamAV) asíncrono.

---

## ADR-007 — JWT emitido por la propia API (HS256)

Contexto. Autenticación stateless simple para SPA móvil.

Decisión. Spring Security OAuth2 Resource Server con `NimbusJwtEncoder/Decoder`, clave ≥ 256 bits desde `JWT_SECRET`, vida 8 h, claim `roles`, validación de emisor.

Alternativas. Keycloak/Entra ID (objetivo institucional); sesiones con cookie.

Consecuencias. (+) Sin estado en servidor, escala horizontalmente. (−) Sin revocación inmediata; clave simétrica. Evolución: IdP institucional con RS256 y refresh tokens.

---

## ADR-008 — Plataforma de despliegue: Render + GitHub Actions + GHCR

Contexto. Publicación obligatoria con URL pública, costo cero, CD automatizado.

Decisión. API como Web Service Docker, front como Static Site, PostgreSQL gestionado de Render. GitHub Actions ejecuta CI y, solo si todo pasa (pruebas, Sonar, Trivy), invoca el deploy hook de Render (auto-deploy desactivado) y verifica `/actuator/health`.

Consecuencias. (+) HTTPS y dominio incluidos; CD controlado por el pipeline. (−) El plan free duerme el servicio tras 15 min (primer request ~1 min) y la BD free expira a los 30 días. Mitigación para la sustentación: abrir la URL de health unos minutos antes.

---

## ADR-009 — Correo por API HTTP (Brevo) en lugar de SMTP

Contexto. Los servicios gratuitos de PaaS como Render restringen el tráfico SMTP saliente.

Decisión. Proveedor configurable `EMAIL_PROVIDER=brevo|smtp`; por defecto Brevo vía `POST /v3/smtp/email`.

Consecuencias. (+) Funciona en el plan gratuito; 300 correos/día. (−) Requiere verificar el remitente en Brevo. (−) Spring Boot registra un chequeo de salud del servidor SMTP que, sin servidor configurado, marca la aplicación como DOWN; se desactiva con `management.health.mail.enabled=false` (variable `MANAGEMENT_HEALTH_MAIL_ENABLED`). Este problema lo detectó el health check de Render durante el primer despliegue.

---

## ADR-010 — WhatsApp mediante plantillas de Twilio (ContentSid)

Contexto. Al desplegar, Twilio rechazó los mensajes de texto libre con el error 21654 "ContentSid Required": la cuenta de prueba solo permite plantillas preaprobadas. Además, WhatsApp exige plantillas aprobadas para cualquier conversación iniciada por la empresa, que es exactamente el caso de UCO Kudos.

Decisión. El adaptador `TwilioWhatsAppNotificationSender` envía la plantilla indicada en `TWILIO_CONTENT_SID` y llena sus variables a partir de `TWILIO_CONTENT_VARIABLES`, un JSON con marcadores `{author}`, `{category}`, `{recipient}` y `{preview}`. Si no hay plantilla configurada, envía texto libre (`Body`), útil en cuentas que lo permitan.

Alternativas. Solo texto libre (no funciona en la cuenta de prueba ni fuera de la ventana de 24 h); SDK oficial de Twilio (más dependencias y superficie de vulnerabilidades).

Consecuencias. (+) Funciona en la cuenta de prueba y es el mecanismo exigido en producción. (+) Cambiar de plantilla es solo configuración (Twelve-Factor III), sin tocar código (OCP). (−) En la cuenta de prueba el texto es el de la plantilla de ejemplo de Twilio, no uno personalizado del Kudo; el correo sí contiene autor, categoría y mensaje. Evolución: número de WhatsApp Business y plantilla propia aprobada por Meta.

---

## ADR-011 — Fijar versiones parcheadas de dependencias transitivas

Contexto. El escaneo de la imagen con Trivy encontró 4 vulnerabilidades CRITICAL en `tomcat-embed-core` 11.0.24 y 5 HIGH en Jackson 3.1.5, dependencias que llegan a través de Spring Boot. El pipeline bloqueó la publicación y el despliegue.

Decisión. Sobrescribir las versiones gestionadas por Spring Boot en el `pom.xml` con las propiedades `tomcat.version` (11.0.25) y `jackson-bom.version` (3.1.7), en lugar de esperar una nueva versión de Spring Boot.

Alternativas. Registrar las CVE en `.trivyignore` (no justificable: había parche y afectaban control de acceso); esperar la siguiente versión de Spring Boot (dejaría el sistema expuesto).

Consecuencias. (+) Imagen sin vulnerabilidades críticas y pipeline en verde. (+) Evidencia del ciclo DevSecOps: detectar, bloquear, corregir, desplegar. (−) Las propiedades deben retirarse cuando Spring Boot incluya esas versiones, para no quedarse fijados en versiones antiguas.
