package co.edu.uco.ucokudos.integration;
import co.edu.uco.ucokudos.domain.audit.AuditAction;
import co.edu.uco.ucokudos.domain.audit.AuditEvent;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import com.jayway.jsonpath.JsonPath;
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
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import co.edu.uco.ucokudos.infrastructure.config.observability.TraceIdFilter;

@SpringBootTest
@ActiveProfiles("test")
class PublishKudoFlowIntegrationTest {

    private static final String PASSWORD = "Prueba2026*";
    private static final UUID STUDENT_ID = UUID.nameUUIDFromBytes("estudiante-demo".getBytes(StandardCharsets.UTF_8));
    private static final UUID TEACHER_ID = UUID.nameUUIDFromBytes("docente@uco.edu.co".getBytes(StandardCharsets.UTF_8));
    private static final String LIDERAZGO_ID = "a1588b1a-5b75-510c-a8a7-90b2192b5c4a";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuditRepository auditRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean(TraceIdFilter.class))
                .apply(springSecurity())
                .build();
    }

    @Test
    void publicarKudoPersisteAuditaYProcesaLosTresCanales() throws Exception {
        String token = login("docente@uco.edu.co");
        var file = new MockMultipartFile("files", "evidencia.png", "image/png", PNG);

        String body = mvc.perform(multipart("/api/v1/kudos").file(file)
                        .param("recipientId", STUDENT_ID.toString())
                        .param("categoryId", LIDERAZGO_ID)
                        .param("content", "Gracias por liderar el semillero")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.recipient.id").value(STUDENT_ID.toString()))
                .andExpect(jsonPath("$.attachments[0].contentType").value("image/png"))
                .andReturn().getResponse().getContentAsString();
        UUID kudoId = UUID.fromString(JsonPath.read(body, "$.id"));

        mvc.perform(get("/api/v1/kudos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(kudoId.toString()));

        List<AuditEvent> trail = waitForAuditEntries(kudoId, 4);
        assertThat(trail).extracting(AuditEvent::action)
                .contains(AuditAction.KUDO_PUBLISHED)
                .filteredOn(action -> action.name().startsWith("NOTIFICATION_")).hasSize(3);
    }

    @Test
    void sinTokenLaApiResponde401() throws Exception {
        mvc.perform(get("/api/v1/kudos")).andExpect(status().isUnauthorized());
    }

    @Test
    void credencialesInvalidasResponden401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"docente@uco.edu.co\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unEstudianteNoPuedeCrearCategorias() throws Exception {
        String token = login("estudiante@uco.edu.co");

        mvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nueva\",\"description\":\"x\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void elRectorSiPuedeCrearCategorias() throws Exception {
        String token = login("rector@uco.edu.co");

        mvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Excelencia Docente\",\"description\":\"Docencia destacada\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @Test
    void noSePuedeDarUnKudoAUnoMismo() throws Exception {
        String token = login("docente@uco.edu.co");

        mvc.perform(multipart("/api/v1/kudos")
                        .param("recipientId", TEACHER_ID.toString())
                        .param("categoryId", LIDERAZGO_ID)
                        .param("content", "Me felicito")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(422));
    }

    @Test
    void rechazaAdjuntosDisfrazados() throws Exception {
        String token = login("docente@uco.edu.co");
        var fakePng = new MockMultipartFile("files", "foto.png", "image/png", new byte[]{'M', 'Z', 0, 0});

        mvc.perform(multipart("/api/v1/kudos").file(fakePng)
                        .param("recipientId", STUDENT_ID.toString())
                        .param("categoryId", LIDERAZGO_ID)
                        .param("content", "Con adjunto falso")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(422));
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private List<AuditEvent> waitForAuditEntries(UUID kudoId, int expected) throws InterruptedException {
        List<AuditEvent> trail = List.of();
        for (int i = 0; i < 100 && trail.size() < expected; i++) {
            trail = auditRepository.findRecent(kudoId, 20);
            Thread.sleep(100);
        }
        return trail;
    }
}
