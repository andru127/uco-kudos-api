package co.edu.uco.ucokudos.infrastructure.config;

import co.edu.uco.ucokudos.infrastructure.config.observability.TraceIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @Test
    void reutilizaUnRequestIdValido() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.REQUEST_HEADER, "abc12345-trace");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(TraceIdFilter.RESPONSE_HEADER)).isEqualTo("abc12345-trace");
    }

    @Test
    void generaUnNuevoIdSiElRecibidoEsInseguro() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.REQUEST_HEADER, "malo\nINFO falso");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(TraceIdFilter.RESPONSE_HEADER)).hasSize(32).doesNotContain("malo");
    }
}
