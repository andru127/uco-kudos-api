package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa;

import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.KudoEntityJPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KudoJPARepository extends JpaRepository<KudoEntityJPA, UUID> {

    /** JOIN FETCH evita el problema N+1 al leer autor, destinatario y categoría del muro. */
    @Query(value = """
            select k from KudoEntityJPA k
            join fetch k.author join fetch k.recipient join fetch k.category
            order by k.publishedAt desc
            """,
            countQuery = "select count(k) from KudoEntityJPA k")
    Page<KudoEntityJPA> findFeed(Pageable pageable);

    @Query("""
            select k from KudoEntityJPA k
            join fetch k.author join fetch k.recipient join fetch k.category
            where k.id = :id
            """)
    Optional<KudoEntityJPA> findDetailedById(@Param("id") UUID id);

    @Query("""
            select new co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.AttachmentMetadata(
                a.id, a.kudo.id, a.fileName, a.contentType, a.sizeBytes, a.sha256)
            from KudoAttachmentEntityJPA a where a.kudo.id in :kudoIds
            """)
    List<AttachmentMetadata> findAttachmentMetadata(@Param("kudoIds") Collection<UUID> kudoIds);
}
