package co.edu.uco.ucokudos.infrastructure.input.rest;

import co.edu.uco.ucokudos.domain.exception.AccessDeniedDomainException;
import co.edu.uco.ucokudos.domain.exception.DomainException;
import co.edu.uco.ucokudos.domain.exception.InvalidCredentialsException;
import co.edu.uco.ucokudos.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final HttpStatusCode UNPROCESSABLE = HttpStatusCode.valueOf(422);
    private static final HttpStatusCode PAYLOAD_TOO_LARGE = HttpStatusCode.valueOf(413);

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleDomain(DomainException e) {
        return problem(UNPROCESSABLE, "Regla de negocio incumplida", e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedDomainException.class)
    ProblemDetail handleForbidden(AccessDeniedDomainException e) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, "No autenticado", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Datos inválidos", detail);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail handleBadParameter(Exception e) {
        return problem(HttpStatus.BAD_REQUEST, "Parámetro inválido", e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail handleTooLarge(MaxUploadSizeExceededException e) {
        return problem(PAYLOAD_TOO_LARGE, "Adjunto demasiado grande", "Cada adjunto admite máximo 5 MB");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception e) {
        if (e instanceof ErrorResponse errorResponse) {
            // Errores propios de Spring MVC (404 de ruta, 405, 415...) conservan su estado HTTP.
            return errorResponse.getBody();
        }
        LOG.error("Error no controlado", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Reporta el X-Trace-Id de la respuesta.");
    }

    private static ProblemDetail problem(HttpStatusCode status, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
