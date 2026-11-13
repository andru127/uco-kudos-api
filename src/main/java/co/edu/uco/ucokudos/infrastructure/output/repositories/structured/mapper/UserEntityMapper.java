package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.mapper;

import co.edu.uco.ucokudos.domain.user.Role;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities.UserEntityJPA;

import java.time.Instant;

public final class UserEntityMapper {

    private UserEntityMapper() {
    }

    public static UserDomain toDomain(UserEntityJPA entity) {
        return new UserDomain(entity.getId(), entity.getFullName(), entity.getEmail(), entity.getPhone(),
                Role.valueOf(entity.getRole()), entity.isActive());
    }

    public static UserEntityJPA toEntity(UserDomain user, String passwordHash, Instant createdAt) {
        return new UserEntityJPA(user.id(), user.fullName(), user.email(), user.phone(), user.role().name(),
                user.active(), passwordHash, createdAt);
    }
}
