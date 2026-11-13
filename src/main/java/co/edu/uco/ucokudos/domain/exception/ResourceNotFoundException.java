package co.edu.uco.ucokudos.domain.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " no encontrado: " + id);
    }
}
