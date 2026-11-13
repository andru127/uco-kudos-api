package co.edu.uco.ucokudos.services.usecases.user;

import co.edu.uco.ucokudos.domain.user.UserDomain;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.usecases.UseCaseWithReturn;

import java.util.List;

public class ListUsersInteractor implements UseCaseWithReturn<Void, List<UserDomain>> {

    private final UserRepository userRepository;

    public ListUsersInteractor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDomain> execute(Void ignored) {
        return userRepository.findAllActive();
    }
}
