package co.edu.uco.ucokudos.infrastructure.config.observability;

import co.edu.uco.ucokudos.infrastructure.output.notifications.push.SseConnectionRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    Gauge sseConnectionsGauge(MeterRegistry registry, SseConnectionRegistry connections) {
        return Gauge.builder("kudos.push.connections", connections, SseConnectionRegistry::activeConnections)
                .description("Conexiones SSE activas")
                .register(registry);
    }
}
