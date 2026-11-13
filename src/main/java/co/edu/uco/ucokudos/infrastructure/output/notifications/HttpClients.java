package co.edu.uco.ucokudos.infrastructure.output.notifications;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

/** Toda llamada a terceros tiene timeout: un proveedor lento no debe bloquear hilos indefinidamente. */
public final class HttpClients {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private HttpClients() {
    }

    public static SimpleClientHttpRequestFactory withTimeouts() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return factory;
    }
}
