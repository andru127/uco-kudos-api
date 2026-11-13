package co.edu.uco.ucokudos.integration;

import com.jayway.jsonpath.JsonPath;
import co.edu.uco.ucokudos.infrastructure.config.observability.TraceIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ApiEndpointsIntegrationTest {

    private static final String PASSWORD = "Prueba2026*";
    private static final UUID STUDENT_ID = UUID.nameUUIDFromBytes("estudiante-demo".getBytes(StandardCharsets.UTF_8));
    private static final String LIDERAZGO_ID = "a1588b1a-5b75-510c-a8a7-90b2192b5c4a";
    private static final byte[] PDF = "%PDF-1.7 contenido de prueba".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean(TraceIdFilter.class))
                .apply(springSecurity())
                .build();
    }

    @Test
    void consultaElPerfilPropioYElDirectorio() throws Exception {
        String token = login("docente@uco.edu.co");

        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("docente@uco.edu.co"))
                .andExpect(jsonPath("$.role").value("DOCENTE"));

        mvc.perform(get("/api/v1/users").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").exists())
                .andExpect(jsonPath("$[0].email").doesNotExist());
    }

    @Test
    void actualizaElContactoYValidaElFormatoDelCelular() throws Exception {
        String token = login("docente@uco.edu.co");

        mvc.perform(put("/api/v1/users/me/contact").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"docente@uco.edu.co\",\"phone\":\"+573009998877\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+573009998877"));

        mvc.perform(put("/api/v1/users/me/contact").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"docente@uco.edu.co\",\"phone\":\"3001234567\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaLasCategoriasYRechazaDuplicados() throws Exception {
        mvc.perform(get("/api/v1/categories").header("Authorization", bearer(login("estudiante@uco.edu.co"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(15)));

        mvc.perform(post("/api/v1/categories").header("Authorization", bearer(login("coord.academico@uco.edu.co")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Liderazgo\",\"description\":\"duplicada\"}"))
                .andExpect(status().is(422));
    }

    @Test
    void publicaConsultaYDescargaElAdjunto() throws Exception {
        String token = login("docente@uco.edu.co");
        String body = mvc.perform(multipart("/api/v1/kudos")
                        .file(new MockMultipartFile("files", "certificado.pdf", "application/pdf", PDF))
                        .param("recipientId", STUDENT_ID.toString())
                        .param("categoryId", LIDERAZGO_ID)
                        .param("content", "Excelente sustentación")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String kudoId = JsonPath.read(body, "$.id");
        String attachmentId = JsonPath.read(body, "$.attachments[0].id");

        mvc.perform(get("/api/v1/kudos/" + kudoId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Excelente sustentación"));

        mvc.perform(get("/api/v1/kudos/" + kudoId + "/attachments/" + attachmentId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(PDF));

        mvc.perform(get("/api/v1/audit").param("resourceId", kudoId)
                        .header("Authorization", bearer(login("rector@uco.edu.co"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resourceId").value(kudoId));
    }

    @Test
    void respondeErroresClarosAnteDatosInvalidos() throws Exception {
        String token = login("docente@uco.edu.co");

        mvc.perform(get("/api/v1/kudos/" + UUID.randomUUID()).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/kudos/no-es-un-uuid").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());

        mvc.perform(multipart("/api/v1/kudos")
                        .param("recipientId", STUDENT_ID.toString())
                        .param("categoryId", LIDERAZGO_ID)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/v1/kudos/" + UUID.randomUUID() + "/attachments/" + UUID.randomUUID())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void soloLosRolesAutorizadosConsultanLaAuditoria() throws Exception {
        mvc.perform(get("/api/v1/audit").header("Authorization", bearer(login("estudiante@uco.edu.co"))))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/audit").header("Authorization", bearer(login("admin@uco.edu.co"))))
                .andExpect(status().isOk());
    }

    @Test
    void abreLaConexionDeNotificacionesEnTiempoReal() throws Exception {
        mvc.perform(get("/api/v1/notifications/stream")
                        .param("access_token", login("estudiante@uco.edu.co"))
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted());
    }

    @Test
    void reutilizaElRequestIdValidoComoTraceId() throws Exception {
        mvc.perform(get("/actuator/health").header("X-Request-Id", "prueba-trace-12345"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "prueba-trace-12345"));
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
