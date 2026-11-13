package co.edu.uco.ucokudos.domain.exception;

/** El actor no tiene permiso para ejecutar la operación según las reglas de negocio. HTTP 403. */
public class AccessDeniedDomainException extends RuntimeException {

    public AccessDeniedDomainException(String message) {
        super(message);
    }
}
