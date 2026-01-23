package br.com.barbearia.apibarbearia.notification.email.catalog;


import br.com.barbearia.apibarbearia.catalog.events.CatalogChangedEvent;
import br.com.barbearia.apibarbearia.catalog.events.CatalogEventType;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class CatalogEmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(CatalogEmailEventListener.class);

    private final UserRepository userRepository;
    private final CatalogEmailNotificationService emailNotificationService;

    public CatalogEmailEventListener(UserRepository userRepository,
                                     CatalogEmailNotificationService emailNotificationService) {
        this.userRepository = userRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCatalogChanged(CatalogChangedEvent event) {
        try {
            String actorName = safeActor(event.getPerformedByName());

            userRepository.findAll().forEach(u -> {
                try {
                    if (u == null) return;
                    if (!u.isActive()) return;
                    if (u.getEmail() == null || u.getEmail().isBlank()) return;

                    dispatch(event, u.getEmail(), u.getName(), actorName);

                } catch (Exception ex) {
                    log.error("Failed sending catalog email to={}", u != null ? u.getEmail() : "null", ex);
                }
            });

        } catch (Exception e) {
            log.error("Failed processing catalog email event. type={}, id={}", event.getType(), event.getItemId(), e);
        }
    }

    private void dispatch(CatalogChangedEvent e, String to, String nome, String actorName) {
        CatalogEventType type = e.getType();

        if (type == CatalogEventType.CREATED) {
            emailNotificationService.sendCatalogCreated(
                    to, nome, e.getName(), e.getDurationMinutes(), e.getPrice().toString(), actorName
            );
            return;
        }

        if (type == CatalogEventType.UPDATED) {
            emailNotificationService.sendCatalogUpdated(
                    to, nome, e.getName(), e.getDurationMinutes(), e.getPrice().toString(), actorName
            );
            return;
        }

        if (type == CatalogEventType.ACTIVATED) {
            emailNotificationService.sendCatalogActivated(
                    to, nome, e.getName(), actorName
            );
            return;
        }

        if (type == CatalogEventType.DEACTIVATED) {
            emailNotificationService.sendCatalogDeactivated(
                    to, nome, e.getName(), actorName
            );
        }
    }

    private String safeActor(String actorName) {
        return (actorName == null || actorName.isBlank()) ? "ADMIN" : actorName;
    }
}
