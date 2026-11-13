package co.edu.uco.ucokudos.domain.exception;

/** El recurso solicitado no existe. Se traduce a HTTP 404 en la capa de entrada. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " no encontrado: " + id);
    }
}
