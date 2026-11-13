package co.edu.uco.ucokudos.services.usecases.user;

import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.util.UUID;

public class GetUserInteractor implements UseCaseWithReturn<UUID, UserDomain> {

    private final UserRepository userRepository;

    public GetUserInteractor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDomain execute(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
    }
}
