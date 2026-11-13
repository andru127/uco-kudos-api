package co.edu.uco.ucokudos.infrastructure.input.rest;

import co.edu.uco.ucokudos.infrastructure.input.rest.dto.UpdateContactRequest;
import co.edu.uco.ucokudos.infrastructure.input.rest.dto.UserResponse;
import co.edu.uco.ucokudos.infrastructure.input.rest.dto.UserSummaryResponse;
import co.edu.uco.ucokudos.services.input.UpdateContactCommand;
import co.edu.uco.ucokudos.services.usecases.user.GetUserInteractor;
import co.edu.uco.ucokudos.services.usecases.user.ListUsersInteractor;
import co.edu.uco.ucokudos.services.usecases.user.UpdateContactInteractor;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserInteractor getUser;
    private final ListUsersInteractor listUsers;
    private final UpdateContactInteractor updateContact;

    public UserController(GetUserInteractor getUser, ListUsersInteractor listUsers,
                          UpdateContactInteractor updateContact) {
        this.getUser = getUser;
        this.listUsers = listUsers;
        this.updateContact = updateContact;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(getUser.execute(CurrentUser.id(jwt)));
    }

    @PutMapping("/me/contact")
    public UserResponse updateMyContact(@AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody UpdateContactRequest request) {
        return UserResponse.from(updateContact.execute(
                new UpdateContactCommand(CurrentUser.id(jwt), request.email(), request.phone())));
    }

    @GetMapping
    public List<UserSummaryResponse> directory() {
        return listUsers.execute(null).stream().map(UserSummaryResponse::from).toList();
    }
}
