# 9. Manual de puesta a punto

Este manual permite a cualquier persona ejecutar UCO Kudos en su equipo y publicarlo en Internet desde cero.

## 9.1 Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| Java JDK | 21 | Back-End (IntelliJ puede descargarlo: File → Project Structure → SDK → Download JDK) |
| Node.js | 22 LTS | Front-End |
| Docker Desktop | reciente | PostgreSQL local (opcional si usas una BD en la nube) |
| Git | reciente | Control de versiones |
| IntelliJ IDEA / VS Code | — | Edición |

Repositorios: `uco-kudos-api` (Back-End + documentación) y `uco-kudos-web` (Front-End).

## 9.2 Ejecución local

### Back-End

```bash
cd uco-kudos-api
docker compose up -d db            # PostgreSQL 17 en localhost:5432
```

En IntelliJ: abre `pom.xml` como proyecto → Run/Debug Configurations → `UcoKudosApiApplication` → Active profiles: `local` → Run.

En terminal:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local      # Windows: mvnw.cmd ...
```

Verifica: <http://localhost:8080/actuator/health> → `{"status":"UP"}`.

Al arrancar se crean las tablas (Flyway), las 15 categorías y un usuario por rol (contraseña `UcoKudos2026*` en perfil local):

| Correo | Rol |
|---|---|
| admin@uco.edu.co | Administrador |
| rector@uco.edu.co | Rector |
| coord.academico@uco.edu.co | Coordinador Académico |
| decano.ingenieria@uco.edu.co | Coordinador de Facultad |
| coord.sistemas@uco.edu.co | Coordinador de Programa |
| docente@uco.edu.co | Docente |
| estudiante@uco.edu.co (o `DEMO_RECIPIENT_EMAIL`) | Estudiante |

Pruebas: `./mvnw verify` (reporte de cobertura en `target/site/jacoco/index.html`).

### Front-End

```bash
cd uco-kudos-web
cp .env.example .env               # VITE_API_URL=http://localhost:8080
npm install
npm run dev                        # http://localhost:5173
npm test                           # pruebas unitarias + cobertura
```

## 9.3 Cuentas de terceros (todas gratuitas)

### Brevo (correo)

1. Crea una cuenta en brevo.com.
2. Configuración → Remitentes, dominios e IP dedicadas → Remitentes: verifica tu correo (Brevo suele agregar automáticamente el correo con el que te registraste).
3. Configuración → SMTP y API → Claves API → Generar una nueva clave API. Elige acceso restringido y concede solo el permiso "Emails transaccionales: Todos" (mínimo privilegio). Guárdala como `BREVO_API_KEY`.
4. `EMAIL_FROM` = el remitente verificado.
5. Las advertencias de DKIM y DMARC aparecen al usar un correo gratuito (Gmail); el envío funciona, pero el correo puede llegar a Spam o Promociones. En producción se usaría un dominio propio con SPF, DKIM y DMARC.

### Twilio (WhatsApp, ambiente de prueba)

1. Crea una cuenta en twilio.com (trial). El celular con el que te registras queda verificado como destinatario.
2. Try it out → Send a WhatsApp message. La pantalla muestra el número de prueba asignado a tu cuenta (en este proyecto `+1 737 250 8034`; puede variar por cuenta) y un código, por ejemplo `join twilio-trial`.
3. Desde el WhatsApp que va a recibir los Kudos envía ese código al número de prueba, o escanea el QR. Twilio responde con un mensaje automático y la pantalla avanza a "Send a message".
4. Restricción de la cuenta de prueba: no permite mensajes de texto libre, solo plantillas preaprobadas por Twilio. En "Send a message" elige una plantilla y abre la pestaña API para copiar su `ContentSid` (empieza por `HX`). Si la plantilla tiene variables (`{{1}}`, `{{2}}`), configura también `TWILIO_CONTENT_VARIABLES`.
5. Copia `Account SID` (pantalla principal de la cuenta) y `Auth Token` (API keys & tokens) → `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`.
6. En producción se registraría un número de WhatsApp Business y una plantilla propia de UCO Kudos aprobada por Meta, con variables de autor y categoría. El código no cambia: solo `TWILIO_CONTENT_SID` y `TWILIO_CONTENT_VARIABLES`, por ejemplo `{"1":"{author}","2":"{category}"}` (marcadores disponibles: `{author}`, `{category}`, `{recipient}`, `{preview}`).

### SonarQube Cloud

1. Entra a sonarcloud.io con GitHub e importa tu organización.
2. Analyze new project → selecciona `uco-kudos-api` y `uco-kudos-web`.
3. En cada proyecto: Administration → Analysis Method → desactiva Automatic Analysis (el análisis lo hace el pipeline).
4. My Account → Security → Generate Token → `SONAR_TOKEN`.
5. Anota la Organization Key y la Project Key de cada proyecto.

## 9.4 Despliegue en Render

### Base de datos

1. New → PostgreSQL (plan Free, misma región para todo, p. ej. Oregon).
2. En la página de la base, sección Connections, copia los campos Hostname, Database, Username y Password y arma:
   - `DATABASE_URL` = `jdbc:postgresql://HOSTNAME:5432/DATABASE` (por ejemplo `jdbc:postgresql://dpg-xxxxxxxx-a:5432/uco_kudos_db`). Debe empezar por `jdbc:postgresql://` y usar el Hostname interno.
   - `DB_USERNAME` = Username
   - `DB_PASSWORD` = Password
3. La API debe estar en la misma región que la base de datos; el hostname interno solo funciona dentro de la misma región.

### API (Web Service)

1. New → Web Service → conecta el repo `uco-kudos-api` → Language: Docker → plan Free.
2. Auto-Deploy: Off (el despliegue lo dispara el pipeline cuando todo pasó).
3. Health Check Path: `/actuator/health`.
4. Variables de entorno:

| Variable | Valor |
|---|---|
| `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` | Del paso anterior |
| `JWT_SECRET` | 48+ caracteres aleatorios (p. ej. `openssl rand -base64 48`) |
| `DEMO_PASSWORD` | Contraseña de los usuarios demo |
| `DEMO_RECIPIENT_EMAIL` | Tu correo (el Estudiante Demo recibirá los Kudos aquí) |
| `DEMO_RECIPIENT_PHONE` | Tu WhatsApp en formato E.164, con indicativo y sin espacios, p. ej. `+573001234567` (el mismo que hizo `join`). Sin el `+57` la aplicación no arranca |
| `CORS_ALLOWED_ORIGINS` | URL del front, p. ej. `https://uco-kudos-web.onrender.com` |
| `FRONTEND_URL` | Igual a la anterior |
| `EMAIL_ENABLED` / `EMAIL_PROVIDER` | `true` / `brevo` |
| `EMAIL_FROM` / `EMAIL_FROM_NAME` / `BREVO_API_KEY` | Remitente verificado, `UCO Kudos` y la clave de Brevo |
| `MANAGEMENT_HEALTH_MAIL_ENABLED` | `false` (con Brevo no hay servidor SMTP; si queda activo, el health check responde DOWN) |
| `WHATSAPP_ENABLED` | `true` |
| `TWILIO_ACCOUNT_SID` / `TWILIO_AUTH_TOKEN` | De Twilio |
| `TWILIO_WHATSAPP_FROM` | Número de prueba de tu cuenta, todo pegado, p. ej. `+17372508034` |
| `TWILIO_CONTENT_SID` | `ContentSid` de la plantilla (`HX...`) |
| `TWILIO_CONTENT_VARIABLES` | Solo si la plantilla tiene variables |
| `LOGGING_STRUCTURED_FORMAT_CONSOLE` | `ecs` (logs JSON) |

5. Settings → Build & Deploy → Deploy Hook → copia la URL (secreto `RENDER_DEPLOY_HOOK_URL` del repo API). Esta URL permite redesplegar el servicio: trátala como secreto.

### Front-End (Static Site)

1. New → Static Site → repo `uco-kudos-web`.
2. Build Command: `npm ci && npm run build` · Publish Directory: `dist` · Auto-Deploy: Off.
3. Variable `VITE_API_URL` = URL pública de la API (sin `/` final).
4. Copia su Deploy Hook (secreto `RENDER_DEPLOY_HOOK_URL` del repo web).
5. Cuando el front esté publicado, actualiza `CORS_ALLOWED_ORIGINS` y `FRONTEND_URL` en la API con su URL y redespliega la API.

URLs del proyecto:

- Front-End: https://uco-kudos-web.onrender.com
- Back-End: https://uco-kudos-api.onrender.com (salud: `/actuator/health`)

## 9.5 Configuración de GitHub Actions

En cada repositorio: Settings → Secrets and variables → Actions.

| Repo | Secrets | Variables |
|---|---|---|
| `uco-kudos-api` | `SONAR_TOKEN`, `RENDER_DEPLOY_HOOK_URL` | `SONAR_ORGANIZATION` (`andru127`), `SONAR_PROJECT_KEY` (`andru127_uco-kudos-api`), `API_URL` |
| `uco-kudos-web` | `SONAR_TOKEN`, `RENDER_DEPLOY_HOOK_URL`, `DEMO_PASSWORD` | `SONAR_ORGANIZATION` (`andru127`), `SONAR_PROJECT_KEY` (`andru127_uco-kudos-web`), `API_URL`, `WEB_URL` |

El mismo `SONAR_TOKEN` sirve para ambos repositorios, porque pertenece al usuario de SonarQube Cloud.

Crea también el entorno `production` (Settings → Environments). Haz un push a `main` y revisa la pestaña Actions.

## 9.6 Verificación final

1. `https://<api>/actuator/health` → `UP`.
2. Abre el front en el celular, entra como `docente@uco.edu.co`.
3. En otro navegador/dispositivo entra con `DEMO_RECIPIENT_EMAIL` (Estudiante Demo) y déjalo abierto.
4. Desde el docente publica un Kudo para Estudiante Demo con un PDF adjunto.
5. Deben llegar: aviso push en la sesión del estudiante, correo y WhatsApp.
6. Envía Kudos de prueba solo al Estudiante Demo: los demás usuarios demo usan direcciones del dominio `uco.edu.co`, y con el correo activo Brevo intentaría entregarlos a buzones reales.
7. Entra como `rector@uco.edu.co` y consulta `GET /api/v1/audit?resourceId=<kudoId>` (o revisa la tabla `audit_events`): 1 `KUDO_PUBLISHED` + 3 `NOTIFICATION_*`.

## 9.7 Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| La primera petición tarda ~1 min | Render free durmió el servicio; abre `/actuator/health` antes de la demo |
| `JWT_SECRET debe tener al menos 32 caracteres` | Falta o es corta la variable |
| Error de CORS en el navegador | `CORS_ALLOWED_ORIGINS` no coincide exactamente con la URL del front (sin `/` final) |
| WhatsApp `FAILED` con error 63016/63015 | El número no hizo `join` o pasaron 24 h sin escribirle al sandbox |
| Correo `FAILED` 401 | `BREVO_API_KEY` inválida; correo no llega → remitente sin verificar o revisa spam |
| Sonar: "automatic analysis is enabled" | Desactívalo en el proyecto de SonarQube Cloud |
| Trivy rompe el pipeline | Hay una CVE CRÍTICA con parche: actualiza la dependencia; si no es explotable regístrala con justificación en `.trivyignore`. Caso real del proyecto: Tomcat 11.0.24 con 4 CVE críticas, corregido fijando `tomcat.version` en 11.0.25 en el `pom.xml` |
| La API falla al arrancar con error de conexión a la base | `DATABASE_URL` mal armada (sin `jdbc:`, con el texto `HOSTNAME` sin reemplazar) o la API en una región distinta a la base |
| La API falla al arrancar justo después de conectar a la base | Error al crear los usuarios demo: casi siempre `DEMO_RECIPIENT_PHONE` sin `+57` |
| Render marca el despliegue como fallido aunque la API arranca | El health check de correo SMTP responde DOWN; agrega `MANAGEMENT_HEALTH_MAIL_ENABLED=false` |
| WhatsApp `FAILED` con código 572002 | El número destino no está verificado en la cuenta de prueba de Twilio |
| WhatsApp `FAILED` con código 21654 "ContentSid Required" | La cuenta de prueba exige plantilla: configura `TWILIO_CONTENT_SID` |
| WhatsApp `FAILED` y el número del sandbox no coincide | `TWILIO_WHATSAPP_FROM` debe ser el número que muestra tu consola, no `+14155238886` por defecto |
| Sonar: Quality Gate FAILED por cobertura | El Quality Gate exige 80 % de cobertura en código nuevo; revisa `target/site/jacoco/index.html` y agrega pruebas |
| Errores `text/event-stream` en los logs al cerrar pestañas | Son conexiones de push cerradas por el cliente; no afectan el funcionamiento |
