package co.edu.uco.ucokudos.infrastructure.output.events;

import co.edu.uco.ucokudos.services.output.ports.events.KudoEventPublisher;
import co.edu.uco.ucokudos.services.output.ports.events.KudoPublishedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Bus de eventos en proceso (línea base). Al estar detrás de un puerto, puede reemplazarse por
 * RabbitMQ/Kafka/SQS sin tocar el caso de uso (ver ADR-003).
 */
@Component
public class SpringKudoEventPublisher implements KudoEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringKudoEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(KudoPublishedEvent event) {
        publisher.publishEvent(event);
    }
}
