package co.edu.uco.ucokudos.services.output.ports.repositories;

import co.edu.uco.ucokudos.domain.kudo.AttachmentDomain;
import co.edu.uco.ucokudos.domain.kudo.KudoDomain;
import co.edu.uco.ucokudos.services.input.PageQuery;
import co.edu.uco.ucokudos.services.input.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface KudoRepository {

    void create(KudoDomain kudo);

    /** Los adjuntos se devuelven solo con metadatos (content = null) para no cargar binarios en lecturas masivas. */
    Optional<KudoDomain> findById(UUID id);

    PageResult<KudoDomain> findFeed(PageQuery query);

    Optional<AttachmentDomain> findAttachment(UUID kudoId, UUID attachmentId);
}
