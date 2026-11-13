package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa;

import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.UserEntityJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJPARepository extends JpaRepository<UserEntityJPA, UUID> {

    Optional<UserEntityJPA> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntityJPA> findByActiveTrueOrderByFullNameAsc();
}
