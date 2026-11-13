# 10. Evidencias

Guarda las capturas en `docs/evidencias/` con el nombre indicado. Cada evidencia incluye qué muestra y cómo obtenerla.

| # | Evidencia | Cómo obtenerla | Archivo |
|---|---|---|---|
| 1 | **CI** Back-End verde | GitHub → `uco-kudos-api` → Actions → último run en `main` | `evidencias/ci-backend.png` |
| 2 | **CI** Front-End verde | Igual en `uco-kudos-web` | `evidencias/ci-frontend.png` |
| 3 | **CD** despliegue disparado y health OK | Job *Desplegar en Render + prueba de humo* expandido | `evidencias/cd-backend.png` |
| 4 | **Sonar** Back-End | SonarQube Cloud → proyecto → Overview (Quality Gate, cobertura, code smells) | `evidencias/sonar-backend.png` |
| 5 | **Sonar** Front-End | Ídem | `evidencias/sonar-frontend.png` |
| 6 | **Trivy** repositorio e imagen | Artefactos `trivy-fs-backend`, `trivy-image-backend`, `trivy-frontend` o log del job | `evidencias/trivy.png` |
| 7 | **Publicación** | Front abierto en el celular con la URL pública visible | `evidencias/publicacion-movil.png` |
| 8 | **Despliegue** | Render → servicio → *Events* (deploy live) | `evidencias/render-deploy.png` |
| 9 | **Correo** recibido | Bandeja de `DEMO_RECIPIENT_EMAIL` | `evidencias/correo.png` |
| 10 | **WhatsApp** recibido | Captura del chat con el sandbox de Twilio | `evidencias/whatsapp.png` |
| 11 | **Push** en tiempo real | Sesión del Estudiante Demo abierta mostrando el toast | `evidencias/push.png` |
| 12 | **Auditoría** | `GET /api/v1/audit?resourceId=<kudoId>` como Rector, o consulta SQL a `audit_events` | `evidencias/auditoria.png` |
| 13 | **Observabilidad** | `/actuator/health` y `/actuator/prometheus` filtrado por `kudos_` + logs de Render con el mismo traceId | `evidencias/observabilidad.png` |
| 14 | **Pruebas** | Resultado de `./mvnw verify` / reporte JaCoCo / `npm test` / reporte Playwright | `evidencias/pruebas.png` |

## Consultar la auditoría por API

```bash
TOKEN=$(curl -s -X POST https://<api>/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"rector@uco.edu.co","password":"<DEMO_PASSWORD>"}' | jq -r .accessToken)
curl -s "https://<api>/api/v1/audit?resourceId=<kudoId>" -H "Authorization: Bearer $TOKEN" | jq
```

## Consulta útil para la auditoría

```sql
SELECT occurred_at, action, detail, trace_id
FROM audit_events
WHERE resource_id = '<kudoId>'
ORDER BY occurred_at;
```

Resultado esperado: `KUDO_PUBLISHED`, `NOTIFICATION_SENT` (EMAIL), `NOTIFICATION_SENT` (WHATSAPP), `NOTIFICATION_SENT` (PUSH) — todos con el **mismo `trace_id`**.

## Galería

<!-- Reemplaza cada línea por la captura correspondiente -->
![CI Back-End](evidencias/ci-backend.png)
![CI Front-End](evidencias/ci-frontend.png)
![CD](evidencias/cd-backend.png)
![Sonar Back-End](evidencias/sonar-backend.png)
![Sonar Front-End](evidencias/sonar-frontend.png)
![Trivy](evidencias/trivy.png)
![Publicación](evidencias/publicacion-movil.png)
![Despliegue](evidencias/render-deploy.png)
![Correo](evidencias/correo.png)
![WhatsApp](evidencias/whatsapp.png)
![Push](evidencias/push.png)
![Auditoría](evidencias/auditoria.png)
![Observabilidad](evidencias/observabilidad.png)
![Pruebas](evidencias/pruebas.png)
