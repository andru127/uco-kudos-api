package co.edu.uco.ucokudos.domain.exception;

public class AccessDeniedDomainException extends RuntimeException {

    public AccessDeniedDomainException(String message) {
        super(message);
    }
}
