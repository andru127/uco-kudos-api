package co.edu.uco.ucokudos.infrastructure.input.rest;

import co.edu.uco.ucokudos.infrastructure.input.rest.dto.LoginRequest;
import co.edu.uco.ucokudos.infrastructure.input.rest.dto.LoginResponse;
import co.edu.uco.ucokudos.infrastructure.input.rest.dto.UserResponse;
import co.edu.uco.ucokudos.services.input.LoginCommand;
import co.edu.uco.ucokudos.services.usecases.auth.LoginInteractor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginInteractor login;

    public AuthController(LoginInteractor login) {
        this.login = login;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var result = login.execute(new LoginCommand(request.email(), request.password()));
        return new LoginResponse(result.accessToken(), "Bearer", result.expiresInSeconds(),
                UserResponse.from(result.user()));
    }
}
