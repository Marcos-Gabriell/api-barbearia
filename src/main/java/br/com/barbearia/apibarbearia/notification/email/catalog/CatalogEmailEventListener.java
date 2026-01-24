package br.com.barbearia.apibarbearia.notification.email.catalog;

import br.com.barbearia.apibarbearia.catalog.events.CatalogChangedEvent;
import br.com.barbearia.apibarbearia.catalog.repository.CatalogRepository;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CatalogEmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(CatalogEmailEventListener.class);

    private final UserRepository userRepository;
    private final CatalogRepository catalogRepository;
    private final CatalogEmailNotificationService emailService;

    public CatalogEmailEventListener(UserRepository userRepository,
                                     CatalogRepository catalogRepository,
                                     CatalogEmailNotificationService emailService) {
        this.userRepository = userRepository;
        this.catalogRepository = catalogRepository;
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCatalogChanged(CatalogChangedEvent event) {
        try {
            List<User> allUsers = userRepository.findAll();

            // Responsáveis ATUAIS (estado final no banco)
            Set<Long> currentResponsibleIds = getResponsibleIds(event.getItemId());

            allUsers.forEach(user -> {
                try {
                    if (user == null || !user.isActive() || user.getEmail() == null) return;

                    processUserEmail(user, event, currentResponsibleIds);

                } catch (Exception ex) {
                    log.error("Failed sending email to user={}", user.getEmail(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Failed processing catalog event type={} id={}", event.getType(), event.getItemId(), e);
        }
    }

    private void processUserEmail(User user, CatalogChangedEvent event, Set<Long> currentResponsibleIds) {
        Long userId = user.getId();
        Long authorId = event.getAdminUserId();

        boolean isAuthor = userId.equals(authorId);

        // Verifica o estado atual e o histórico (diff)
        boolean isCurrentResponsible = currentResponsibleIds.contains(userId);
        boolean wasAdded = event.getAddedResponsibleIds().contains(userId);
        boolean wasRemoved = event.getRemovedResponsibleIds().contains(userId);

        switch (event.getType()) {
            case CREATED:
                if (isAuthor) {
                    emailService.sendCreatedToAuthor(user, event);
                } else if (isCurrentResponsible) {
                    emailService.sendCreatedToResponsible(user, event);
                } else {
                    emailService.sendCreatedToOthers(user, event);
                }
                break;

            case UPDATED:
                if (isAuthor) {
                    // Autor sempre recebe confirmação de edição
                    emailService.sendUpdatedToAuthor(user, event);
                } else if (wasAdded) {
                    // NOVO: Recebe boas-vindas ao serviço (mesmo layout de Created Responsible)
                    emailService.sendAddedAsResponsible(user, event);
                } else if (wasRemoved) {
                    // NOVO: Recebe aviso de remoção
                    emailService.sendRemovedAsResponsible(user, event);
                } else if (isCurrentResponsible) {
                    // Já era e continua sendo: recebe aviso de atualização
                    emailService.sendUpdatedToResponsible(user, event);
                }
                // 'Others' (demais usuários) NÃO recebem e-mail de Update para evitar spam
                break;

            case DELETED:
                if (isAuthor) {
                    emailService.sendDeletedToAuthor(user, event);
                } else if (isCurrentResponsible || wasRemoved) {
                    // Se foi removido no processo de delete ou era responsável
                    emailService.sendDeletedToResponsible(user, event);
                } else {
                    emailService.sendDeletedToOthers(user, event);
                }
                break;

            case ACTIVATED:
            case DEACTIVATED:
                if (isAuthor || isCurrentResponsible) {
                    emailService.sendStatusChanged(user, event);
                }
                break;
        }
    }

    private Set<Long> getResponsibleIds(Long itemId) {
        return catalogRepository.findById(itemId)
                .map(item -> item.getResponsibles().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }
}