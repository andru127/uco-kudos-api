package co.edu.uco.ucokudos.services.output.ports.events;

import co.edu.uco.ucokudos.domain.notification.KudoNotification;

/** Evento de dominio: un Kudo quedó persistido y debe notificarse al destinatario. */
public record KudoPublishedEvent(KudoNotification notification) {
}
