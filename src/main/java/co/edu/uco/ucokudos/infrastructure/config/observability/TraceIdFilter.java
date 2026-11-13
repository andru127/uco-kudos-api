package co.edu.uco.ucokudos.infrastructure.config.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Asigna un traceId a cada petición (o reutiliza X-Request-Id si es válido), lo pone en el MDC para que
 * aparezca en todos los logs y en la auditoría, y lo devuelve en la cabecera X-Trace-Id.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    public static final String REQUEST_HEADER = "X-Request-Id";
    public static final String RESPONSE_HEADER = "X-Trace-Id";
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9-]{8,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String incoming = request.getHeader(REQUEST_HEADER);
        // Se valida para evitar inyección de logs con valores arbitrarios del cliente.
        String traceId = incoming != null && SAFE_ID.matcher(incoming).matches()
                ? incoming : UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID, traceId);
        response.setHeader(RESPONSE_HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
