package co.edu.uco.ucokudos.infrastructure.input.rest;

import co.edu.uco.ucokudos.infrastructure.input.rest.dto.KudoResponse;
import co.edu.uco.ucokudos.infrastructure.input.rest.dto.PageResponse;
import co.edu.uco.ucokudos.services.input.AttachmentQuery;
import co.edu.uco.ucokudos.services.input.PageQuery;
import co.edu.uco.ucokudos.services.input.PublishKudoCommand;
import co.edu.uco.ucokudos.services.usecases.kudo.GetAttachmentInteractor;
import co.edu.uco.ucokudos.services.usecases.kudo.GetKudoInteractor;
import co.edu.uco.ucokudos.services.usecases.kudo.ListKudoFeedInteractor;
import co.edu.uco.ucokudos.services.usecases.kudo.PublishKudoInteractor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kudos")
public class KudoController {

    private final PublishKudoInteractor publishKudo;
    private final ListKudoFeedInteractor listKudoFeed;
    private final GetKudoInteractor getKudo;
    private final GetAttachmentInteractor getAttachment;

    public KudoController(PublishKudoInteractor publishKudo, ListKudoFeedInteractor listKudoFeed,
                          GetKudoInteractor getKudo, GetAttachmentInteractor getAttachment) {
        this.publishKudo = publishKudo;
        this.listKudoFeed = listKudoFeed;
        this.getKudo = getKudo;
        this.getAttachment = getAttachment;
    }

    /** Flujo obligatorio de la línea base. multipart/form-data para admitir adjuntos. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KudoResponse> publish(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam UUID recipientId,
                                                @RequestParam UUID categoryId,
                                                @RequestParam String content,
                                                @RequestParam(name = "files", required = false) List<MultipartFile> files) {
        var command = new PublishKudoCommand(CurrentUser.id(jwt), recipientId, categoryId, content, toInputs(files));
        var kudo = publishKudo.execute(command);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(kudo.id()).toUri();
        return ResponseEntity.created(location).body(KudoResponse.from(kudo));
    }

    @GetMapping
    public PageResponse<KudoResponse> feed(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return PageResponse.from(listKudoFeed.execute(new PageQuery(page, size)), KudoResponse::from);
    }

    @GetMapping("/{kudoId}")
    public KudoResponse detail(@PathVariable UUID kudoId) {
        return KudoResponse.from(getKudo.execute(kudoId));
    }

    @GetMapping("/{kudoId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID kudoId, @PathVariable UUID attachmentId) {
        var attachment = getAttachment.execute(new AttachmentQuery(kudoId, attachmentId));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(attachment.fileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(attachment.content());
    }

    private static List<PublishKudoCommand.AttachmentInput> toInputs(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream()
                .filter(file -> !file.isEmpty())
                .map(KudoController::toInput)
                .toList();
    }

    private static PublishKudoCommand.AttachmentInput toInput(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String name = file.getOriginalFilename() == null ? "adjunto" : file.getOriginalFilename();
            return new PublishKudoCommand.AttachmentInput(name, FileTypeDetector.detect(bytes), bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible leer el adjunto", e);
        }
    }
}
