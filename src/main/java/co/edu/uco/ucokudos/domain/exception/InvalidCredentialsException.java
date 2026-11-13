package co.edu.uco.ucokudos.domain.exception;

/** Credenciales inválidas en el inicio de sesión. HTTP 401. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
