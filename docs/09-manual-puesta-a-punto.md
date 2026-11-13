# 9. Manual de puesta a punto

Este manual permite a cualquier persona ejecutar UCO Kudos en su equipo y publicarlo en Internet desde cero.

## 9.1 Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| Java JDK | 21 | Back-End (IntelliJ puede descargarlo: *File → Project Structure → SDK → Download JDK*) |
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

**En IntelliJ:** abre `pom.xml` como proyecto → *Run/Debug Configurations* → `UcoKudosApiApplication` → *Active profiles*: `local` → Run.

**En terminal:**

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
2. *Senders, Domains & Dedicated IPs → Senders → Add a sender*: registra tu correo (p. ej. tu Gmail) y confírmalo desde el correo que te llega.
3. *SMTP & API → API Keys → Generate a new API key*. Guárdala como `BREVO_API_KEY`.
4. `EMAIL_FROM` = el remitente verificado.

### Twilio (WhatsApp sandbox)

1. Crea una cuenta en twilio.com (trial).
2. *Messaging → Try it out → Send a WhatsApp message*. Verás el número del sandbox (normalmente `+14155238886`) y un código `join <palabra-palabra>`.
3. **Desde el WhatsApp que va a recibir los Kudos** envía `join <palabra-palabra>` a ese número. ⚠️ El sandbox solo entrega a números unidos, y los mensajes libres solo dentro de las 24 h siguientes al último mensaje que tú le enviaste: **escríbele cualquier cosa al sandbox el día de la sustentación**.
4. Copia `Account SID` y `Auth Token` de la consola → `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`.

### SonarQube Cloud

1. Entra a sonarcloud.io con GitHub e importa tu organización.
2. *Analyze new project* → selecciona `uco-kudos-api` y `uco-kudos-web`.
3. En cada proyecto: *Administration → Analysis Method* → **desactiva Automatic Analysis** (el análisis lo hace el pipeline).
4. *My Account → Security → Generate Token* → `SONAR_TOKEN`.
5. Anota la *Organization Key* y la *Project Key* de cada proyecto.

## 9.4 Despliegue en Render

### Base de datos

1. *New → PostgreSQL* (plan Free, misma región para todo, p. ej. Oregon).
2. De *Connections → Internal Database URL* (`postgresql://USUARIO:CLAVE@HOST/BASE`) arma:
   - `DATABASE_URL` = `jdbc:postgresql://HOST:5432/BASE`
   - `DB_USERNAME` = `USUARIO`
   - `DB_PASSWORD` = `CLAVE`

### API (Web Service)

1. *New → Web Service* → conecta el repo `uco-kudos-api` → *Language: Docker* → plan Free.
2. *Auto-Deploy*: **Off** (el despliegue lo dispara el pipeline cuando todo pasó).
3. *Health Check Path*: `/actuator/health`.
4. Variables de entorno:

| Variable | Valor |
|---|---|
| `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` | Del paso anterior |
| `JWT_SECRET` | 48+ caracteres aleatorios (p. ej. `openssl rand -base64 48`) |
| `DEMO_PASSWORD` | Contraseña de los usuarios demo |
| `DEMO_RECIPIENT_EMAIL` | **Tu correo** (el Estudiante Demo recibirá los Kudos aquí) |
| `DEMO_RECIPIENT_PHONE` | **Tu WhatsApp** en E.164, p. ej. `+573001234567` (el mismo que hizo `join`) |
| `CORS_ALLOWED_ORIGINS` | URL del front, p. ej. `https://uco-kudos-web.onrender.com` |
| `FRONTEND_URL` | Igual a la anterior |
| `EMAIL_ENABLED` / `EMAIL_PROVIDER` | `true` / `brevo` |
| `EMAIL_FROM` / `BREVO_API_KEY` | De Brevo |
| `WHATSAPP_ENABLED` | `true` |
| `TWILIO_ACCOUNT_SID` / `TWILIO_AUTH_TOKEN` / `TWILIO_WHATSAPP_FROM` | De Twilio (`+14155238886`) |
| `LOGGING_STRUCTURED_FORMAT_CONSOLE` | `ecs` (logs JSON) |

5. *Settings → Deploy Hook* → copia la URL (secreto `RENDER_DEPLOY_HOOK_URL` del repo API).

### Front-End (Static Site)

1. *New → Static Site* → repo `uco-kudos-web`.
2. *Build Command*: `npm ci && npm run build` · *Publish Directory*: `dist` · *Auto-Deploy*: Off.
3. Variable `VITE_API_URL` = URL pública de la API (sin `/` final).
4. Copia su *Deploy Hook* (secreto `RENDER_DEPLOY_HOOK_URL` del repo web).

## 9.5 Configuración de GitHub Actions

En cada repositorio: *Settings → Secrets and variables → Actions*.

| Repo | Secrets | Variables |
|---|---|---|
| `uco-kudos-api` | `SONAR_TOKEN`, `RENDER_DEPLOY_HOOK_URL` | `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY`, `API_URL` |
| `uco-kudos-web` | `SONAR_TOKEN`, `RENDER_DEPLOY_HOOK_URL`, `DEMO_PASSWORD` | `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY`, `API_URL`, `WEB_URL` |

Crea también el entorno `production` (*Settings → Environments*). Haz un push a `main` y revisa la pestaña *Actions*.

## 9.6 Verificación final

1. `https://<api>/actuator/health` → `UP`.
2. Abre el front en el celular, entra como `docente@uco.edu.co`.
3. En otro navegador/dispositivo entra con `DEMO_RECIPIENT_EMAIL` (Estudiante Demo) y déjalo abierto.
4. Desde el docente publica un Kudo para *Estudiante Demo* con un PDF adjunto.
5. Deben llegar: toast push en la sesión del estudiante, correo y WhatsApp.
6. Entra como `rector@uco.edu.co` y consulta `GET /api/v1/audit?resourceId=<kudoId>` (o revisa la tabla `audit_events`): 1 `KUDO_PUBLISHED` + 3 `NOTIFICATION_*`.

## 9.7 Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| La primera petición tarda ~1 min | Render free durmió el servicio; abre `/actuator/health` antes de la demo |
| `JWT_SECRET debe tener al menos 32 caracteres` | Falta o es corta la variable |
| Error de CORS en el navegador | `CORS_ALLOWED_ORIGINS` no coincide exactamente con la URL del front (sin `/` final) |
| WhatsApp `FAILED` con error 63016/63015 | El número no hizo `join` o pasaron 24 h sin escribirle al sandbox |
| Correo `FAILED` 401 | `BREVO_API_KEY` inválida; correo no llega → remitente sin verificar o revisa spam |
| Sonar: "automatic analysis is enabled" | Desactívalo en el proyecto de SonarQube Cloud |
| Trivy rompe el pipeline | Hay una CVE CRÍTICA con parche: actualiza la dependencia; si no es explotable regístrala con justificación en `.trivyignore` |
