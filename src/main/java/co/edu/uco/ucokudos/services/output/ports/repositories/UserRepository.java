package co.edu.uco.ucokudos.services.output.ports.repositories;

import co.edu.uco.ucokudos.domain.user.UserDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<UserDomain> findById(UUID id);

    Optional<UserCredentials> findCredentialsByEmail(String email);

    List<UserDomain> findAllActive();

    boolean existsByEmail(String email);

    void create(UserDomain user, String passwordHash);

    void updateContact(UserDomain user);

    record UserCredentials(UserDomain user, String passwordHash) {
    }
}
