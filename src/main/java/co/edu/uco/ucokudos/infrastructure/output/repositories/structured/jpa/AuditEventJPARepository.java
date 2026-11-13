package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa;

import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.AuditEventEntityJPA;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventJPARepository extends JpaRepository<AuditEventEntityJPA, UUID> {

    List<AuditEventEntityJPA> findByResourceIdOrderByOccurredAtDesc(UUID resourceId, Pageable pageable);

    List<AuditEventEntityJPA> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
