package co.edu.uco.ucokudos.infrastructure.output.repositories.structured;

import co.edu.uco.ucokudos.domain.kudo.AttachmentDomain;
import co.edu.uco.ucokudos.domain.kudo.KudoDomain;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.AttachmentMetadata;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.KudoAttachmentJPARepository;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.KudoJPARepository;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.CategoryEntityJPA;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.KudoAttachmentEntityJPA;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.KudoEntityJPA;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.UserEntityJPA;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.mapper.KudoEntityMapper;
import co.edu.uco.ucokudos.services.input.PageQuery;
import co.edu.uco.ucokudos.services.input.PageResult;
import co.edu.uco.ucokudos.services.output.ports.repositories.KudoRepository;
import jakarta.persistence.EntityManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class KudoRepositoryAdapter implements KudoRepository {

    /** Caché del muro (DRV-002). TTL corto + invalidación al publicar. */
    public static final String FEED_CACHE = "kudoFeed";

    private final KudoJPARepository kudoRepository;
    private final KudoAttachmentJPARepository attachmentRepository;
    private final EntityManager entityManager;

    public KudoRepositoryAdapter(KudoJPARepository kudoRepository, KudoAttachmentJPARepository attachmentRepository,
                                 EntityManager entityManager) {
        this.kudoRepository = kudoRepository;
        this.attachmentRepository = attachmentRepository;
        this.entityManager = entityManager;
    }

    @Override
    @CacheEvict(cacheNames = FEED_CACHE, allEntries = true)
    @Transactional
    public void create(KudoDomain kudo) {
        var entity = new KudoEntityJPA(kudo.id(),
                entityManager.getReference(UserEntityJPA.class, kudo.author().id()),
                entityManager.getReference(UserEntityJPA.class, kudo.recipient().id()),
                entityManager.getReference(CategoryEntityJPA.class, kudo.category().id()),
                kudo.content(), kudo.publishedAt());
        for (AttachmentDomain attachment : kudo.attachments()) {
            entity.addAttachment(new KudoAttachmentEntityJPA(attachment.id(), entity, attachment.fileName(),
                    attachment.contentType(), attachment.sizeBytes(), attachment.sha256(), attachment.content()));
        }
        // persist (y no save/merge): el id lo genera el dominio, así evitamos un SELECT previo.
        entityManager.persist(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KudoDomain> findById(UUID id) {
        return kudoRepository.findDetailedById(id)
                .map(entity -> KudoEntityMapper.toDomain(entity, kudoRepository.findAttachmentMetadata(List.of(id))));
    }

    @Override
    @Cacheable(cacheNames = FEED_CACHE, key = "#query.page() + ':' + #query.size()")
    @Transactional(readOnly = true)
    public PageResult<KudoDomain> findFeed(PageQuery query) {
        var page = kudoRepository.findFeed(PageRequest.of(query.page(), query.size()));
        var ids = page.getContent().stream().map(KudoEntityJPA::getId).toList();
        Map<UUID, List<AttachmentMetadata>> attachmentsByKudo = ids.isEmpty()
                ? Map.of()
                : kudoRepository.findAttachmentMetadata(ids).stream()
                    .collect(Collectors.groupingBy(AttachmentMetadata::kudoId));
        var items = page.getContent().stream()
                .map(entity -> KudoEntityMapper.toDomain(entity, attachmentsByKudo.getOrDefault(entity.getId(), List.of())))
                .toList();
        return new PageResult<>(items, query.page(), query.size(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttachmentDomain> findAttachment(UUID kudoId, UUID attachmentId) {
        return attachmentRepository.findByIdAndKudoId(attachmentId, kudoId).map(KudoEntityMapper::toDomain);
    }
}
