package co.edu.uco.ucokudos.infrastructure.output.events;

import co.edu.uco.ucokudos.services.output.ports.events.KudoEventPublisher;
import co.edu.uco.ucokudos.services.output.ports.events.KudoPublishedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

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
