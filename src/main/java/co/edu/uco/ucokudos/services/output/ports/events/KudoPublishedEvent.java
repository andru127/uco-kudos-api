package co.edu.uco.ucokudos.services.output.ports.events;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;

public record KudoPublishedEvent(KudoNotification notification) {
}
