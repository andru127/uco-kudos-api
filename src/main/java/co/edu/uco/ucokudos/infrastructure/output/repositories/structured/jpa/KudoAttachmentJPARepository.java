package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa;

import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.KudoAttachmentEntityJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KudoAttachmentJPARepository extends JpaRepository<KudoAttachmentEntityJPA, UUID> {

    Optional<KudoAttachmentEntityJPA> findByIdAndKudoId(UUID id, UUID kudoId);
}
