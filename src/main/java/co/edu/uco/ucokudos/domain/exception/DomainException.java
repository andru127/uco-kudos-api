package co.edu.uco.ucokudos.domain.exception;

/** Violación de una regla de negocio. Se traduce a HTTP 422 en la capa de entrada. */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
