package co.edu.uco.ucokudos.infrastructure.output.repositories.structured;

import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.UserJPARepository;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJPARepository repository;
    private final Clock clock;

    public UserRepositoryAdapter(UserJPARepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDomain> findById(UUID id) {
        return repository.findById(id).map(UserEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCredentials> findCredentialsByEmail(String email) {
        return repository.findByEmail(email)
                .map(entity -> new UserCredentials(UserEntityMapper.toDomain(entity), entity.getPasswordHash()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDomain> findAllActive() {
        return repository.findByActiveTrueOrderByFullNameAsc().stream().map(UserEntityMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void create(UserDomain user, String passwordHash) {
        repository.save(UserEntityMapper.toEntity(user, passwordHash, clock.instant()));
    }

    @Override
    @Transactional
    public void updateContact(UserDomain user) {
        var entity = repository.findById(user.id()).orElseThrow(() -> new ResourceNotFoundException("Usuario", user.id()));
        entity.changeContact(user.email(), user.phone());
    }
}
