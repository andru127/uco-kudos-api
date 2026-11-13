package co.edu.uco.ucokudos.services.output.ports.events;

public interface KudoEventPublisher {

    void publish(KudoPublishedEvent event);
}
