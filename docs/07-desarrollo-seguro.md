# 7. Desarrollo seguro

## 7.1 Autenticación

- Inicio de sesión con correo institucional y contraseña; contraseñas con **BCrypt** (sal por usuario, costo adaptable).
- Se emite un **JWT HS256** (clave ≥ 256 bits desde `JWT_SECRET`) con `iss`, `sub` (id de usuario), `exp` (8 h) y `roles`. Se valida firma, emisor y expiración en cada petición.
- **Mensaje genérico** "Credenciales inválidas" tanto si el correo no existe como si la contraseña está mal (evita enumeración de usuarios). Todo intento queda auditado (`LOGIN_SUCCEEDED` / `LOGIN_FAILED`).
- Front-End: el token vive en `sessionStorage` (se borra al cerrar la pestaña) y ante un 401 se cierra la sesión automáticamente.

## 7.2 Autorización

Defensa en profundidad, dos capas:

| Capa | Mecanismo | Ejemplo |
|---|---|---|
| Perímetro HTTP | `SecurityFilterChain`: rutas públicas mínimas (login, health, prometheus); `hasAnyRole` en rutas sensibles | `POST /api/v1/categories` solo ADMINISTRADOR, RECTOR, COORDINADOR_ACADEMICO |
| Negocio | Reglas en el dominio (`Role.canManageCategories()`, `canVetoContent()`, `canReadAudit()`) evaluadas en el caso de uso | `CreateCategoryInteractor` vuelve a validar el rol aunque el filtro ya lo hizo |

El autor de un Kudo siempre se toma del token (`sub`), **nunca** del cuerpo de la petición (evita suplantación). El directorio público de usuarios (`UserSummaryResponse`) no expone correo ni celular de terceros.

## 7.3 Gestión de secretos

- Ningún secreto en el código ni en las migraciones: `JWT_SECRET`, `DB_PASSWORD`, `BREVO_API_KEY`, `TWILIO_AUTH_TOKEN`, `DEMO_PASSWORD`, `SONAR_TOKEN`, `RENDER_DEPLOY_HOOK_URL` viven en **Render Environment** y **GitHub Secrets**.
- Si falta un secreto obligatorio la aplicación no arranca (*fail fast*).
- Trivy ejecuta el escáner `secret` en cada pipeline para detectar credenciales filtradas.
- `.gitignore` excluye `.env`. El perfil `local` contiene solo valores de desarrollo sin valor real.

## 7.4 Validaciones

| Dónde | Qué |
|---|---|
| DTO (Bean Validation) | `@NotBlank`, `@Email`, `@Size`, `@Pattern` (E.164) en login, contacto y categorías |
| Dominio | Contenido no vacío, no auto-Kudo, usuarios y categorías activos, máx. 5 adjuntos, tipo y tamaño de archivo |
| Archivos | Tipo real por **magic bytes** (`FileTypeDetector`), no por extensión ni `Content-Type`; nombre saneado contra *path traversal*; 5 MB por archivo y 26 MB por petición |
| Salida | Todo dato de usuario se escapa al construir el HTML del correo (`HtmlUtils.htmlEscape`); React escapa por defecto en la interfaz |
| Paginación | `size` acotado a 50 (evita consultas abusivas) |

## 7.5 APIs

- Versionadas (`/api/v1`), stateless, HTTPS (Render).
- Errores con **Problem Details (RFC 9457)**, sin stack traces; los errores inesperados devuelven un mensaje genérico y se registran con el `X-Trace-Id`.
- **CORS** restringido a los orígenes de `CORS_ALLOWED_ORIGINS`.
- CSRF deshabilitado de forma justificada: no hay cookies de sesión (el token viaja en `Authorization`).
- Cabeceras de seguridad por defecto de Spring Security (`X-Content-Type-Options`, `X-Frame-Options: DENY`, `Cache-Control`); descargas con `Content-Disposition: attachment`, `nosniff` y `no-store`.
- El `X-Request-Id` entrante se valida con una expresión regular antes de usarlo en logs (previene *log injection*).

## 7.6 Integraciones con terceros

| Integración | Controles |
|---|---|
| Brevo / Twilio | Solo HTTPS; credenciales por entorno; **timeouts** (5 s conexión, 10 s lectura); reintentos acotados; sin SDKs (menos dependencias = menos superficie para CVE); el celular se **enmascara** en auditoría y logs |
| SonarQube Cloud | Token en GitHub Secrets |
| Trivy | Imagen fijada a **0.69.3**, versión confirmada como segura tras el compromiso de la cadena de suministro de marzo de 2026 (CVE-2026-33634). No se usan las *GitHub Actions* de Trivy cuyos tags fueron secuestrados. |
| GitHub Actions | Permisos mínimos (`contents: read`, `packages: write`); entorno `production` para el despliegue |

## 7.7 Estrategia para archivos adjuntos

| Estrategia | Línea base | Objetivo |
|---|---|---|
| **Almacenamiento** | `bytea` en PostgreSQL, metadatos separados, proyección sin binario para lecturas | Object storage (S3/R2) con URLs prefirmadas de 5 min |
| **Seguridad** | Lista blanca por firma, límite de tamaño y cantidad, nombre saneado, descarga solo autenticada, `nosniff`, `attachment` | + Antivirus (ClamAV) asíncrono y cuarentena |
| **Recuperación** | Respaldo diario automático de la BD gestionada (incluye adjuntos) | Versionado del bucket + replicación entre regiones |
| **Trazabilidad** | SHA-256 por archivo (integridad verificable), cantidad de adjuntos en la auditoría `KUDO_PUBLISHED`, traceId | Auditoría de cada descarga |

## 7.8 Riesgos de seguridad (OWASP Top 10) y mitigaciones

| Riesgo | Mitigación | Riesgo residual |
|---|---|---|
| A01 Control de acceso roto | RBAC en dos capas; autor tomado del token | Bajo |
| A02 Fallas criptográficas | BCrypt, HS256 ≥ 256 bits, HTTPS | Clave simétrica (ADR-007) |
| A03 Inyección | JPA con parámetros; escape de HTML; validación de traceId | Bajo |
| A04 Diseño inseguro | Reglas en el dominio; *threat modeling* de adjuntos | Medio: sin MFA |
| A05 Configuración insegura | Trivy `misconfig`; contenedor sin root; Actuator limitado | `/actuator/prometheus` público para evidencias (restringir en producción real) |
| A06 Componentes vulnerables | Trivy (repo e imagen) bloquea CRÍTICAS con parche; Sonar | Depende del ritmo de parches |
| A07 Fallas de autenticación | Mensaje genérico, auditoría de intentos | Sin rate limiting ni bloqueo por intentos (pendiente: API Gateway / Bucket4j) |
| A08 Integridad de software | Imagen de Trivy fijada; SHA-256 de adjuntos; imagen etiquetada por commit | Sin firma de imágenes (Cosign) |
| A09 Registro y monitoreo | Auditoría + logs con traceId + métricas | Logs efímeros en plan free |
| A10 SSRF | Las URL de terceros son fijas por configuración, nunca provienen del usuario | Bajo |
| Token en query string (SSE) | Solo en GET de stream, TTL corto, HTTPS | Puede quedar en logs de proxy; evolución: ticket de un solo uso |
