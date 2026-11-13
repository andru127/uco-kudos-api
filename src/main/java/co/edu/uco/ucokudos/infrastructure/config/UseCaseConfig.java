package co.edu.uco.ucokudos.infrastructure.config;

import co.edu.uco.ucokudos.infrastructure.output.notifications.NotificationProperties;
import co.edu.uco.ucokudos.infrastructure.output.notifications.ResilientNotificationSender;
import co.edu.uco.ucokudos.services.output.ports.events.KudoEventPublisher;
import co.edu.uco.ucokudos.services.output.ports.notifications.NotificationSender;
import co.edu.uco.ucokudos.services.output.ports.repositories.AuditRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.CategoryRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.KudoRepository;
import co.edu.uco.ucokudos.services.output.ports.repositories.UserRepository;
import co.edu.uco.ucokudos.services.output.ports.security.PasswordHasher;
import co.edu.uco.ucokudos.services.output.ports.security.TokenIssuer;
import co.edu.uco.ucokudos.services.output.ports.transaction.TransactionRunner;
import co.edu.uco.ucokudos.services.usecases.audit.ListAuditInteractor;
import co.edu.uco.ucokudos.services.usecases.auth.LoginInteractor;
import co.edu.uco.ucokudos.services.usecases.category.CreateCategoryInteractor;
import co.edu.uco.ucokudos.services.usecases.category.ListCategoriesInteractor;
import co.edu.uco.ucokudos.services.usecases.kudo.GetAttachmentInteractor;
import co.edu.uco.ucokudos.services.usecases.kudo.GetKudoInteractor;
import co.edu.uco.ucokudos.services.usecases.kudo.ListKudoFeedInteractor;
import co.edu.uco.ucokudos.services.usecases.kudo.PublishKudoInteractor;
import co.edu.uco.ucokudos.services.usecases.notification.NotifyKudoRecipientInteractor;
import co.edu.uco.ucokudos.services.usecases.user.GetUserInteractor;
import co.edu.uco.ucokudos.services.usecases.user.ListUsersInteractor;
import co.edu.uco.ucokudos.services.usecases.user.UpdateContactInteractor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

/**
 * Composition Root: el único lugar que conoce a la vez los casos de uso y sus implementaciones.
 * Gracias a esto la capa services no tiene ni una anotación de Spring (regla de dependencia de Clean Architecture).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    PublishKudoInteractor publishKudoInteractor(UserRepository users, CategoryRepository categories,
                                                KudoRepository kudos, AuditRepository audit,
                                                KudoEventPublisher events, TransactionRunner tx, Clock clock) {
        return new PublishKudoInteractor(users, categories, kudos, audit, events, tx, clock);
    }

    @Bean
    NotifyKudoRecipientInteractor notifyKudoRecipientInteractor(List<NotificationSender> senders,
                                                                AuditRepository audit,
                                                                NotificationProperties properties,
                                                                MeterRegistry meterRegistry, Clock clock) {
        List<NotificationSender> resilientSenders = senders.stream()
                .<NotificationSender>map(sender -> new ResilientNotificationSender(sender, meterRegistry,
                        properties.retry().maxAttempts(), properties.retry().backoffMillis()))
                .toList();
        return new NotifyKudoRecipientInteractor(resilientSenders, audit, clock);
    }

    @Bean
    ListKudoFeedInteractor listKudoFeedInteractor(KudoRepository kudos) {
        return new ListKudoFeedInteractor(kudos);
    }

    @Bean
    GetKudoInteractor getKudoInteractor(KudoRepository kudos) {
        return new GetKudoInteractor(kudos);
    }

    @Bean
    GetAttachmentInteractor getAttachmentInteractor(KudoRepository kudos) {
        return new GetAttachmentInteractor(kudos);
    }

    @Bean
    LoginInteractor loginInteractor(UserRepository users, PasswordHasher hasher, TokenIssuer tokens,
                                    AuditRepository audit, Clock clock) {
        return new LoginInteractor(users, hasher, tokens, audit, clock);
    }

    @Bean
    ListCategoriesInteractor listCategoriesInteractor(CategoryRepository categories) {
        return new ListCategoriesInteractor(categories);
    }

    @Bean
    CreateCategoryInteractor createCategoryInteractor(UserRepository users, CategoryRepository categories,
                                                      AuditRepository audit, Clock clock) {
        return new CreateCategoryInteractor(users, categories, audit, clock);
    }

    @Bean
    GetUserInteractor getUserInteractor(UserRepository users) {
        return new GetUserInteractor(users);
    }

    @Bean
    ListUsersInteractor listUsersInteractor(UserRepository users) {
        return new ListUsersInteractor(users);
    }

    @Bean
    UpdateContactInteractor updateContactInteractor(UserRepository users, AuditRepository audit, Clock clock) {
        return new UpdateContactInteractor(users, audit, clock);
    }

    @Bean
    ListAuditInteractor listAuditInteractor(UserRepository users, AuditRepository audit) {
        return new ListAuditInteractor(users, audit);
    }
}
