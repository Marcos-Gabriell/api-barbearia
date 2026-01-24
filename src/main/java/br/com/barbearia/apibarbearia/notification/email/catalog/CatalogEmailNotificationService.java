package br.com.barbearia.apibarbearia.notification.email.catalog;

import br.com.barbearia.apibarbearia.catalog.events.CatalogChangedEvent;
import br.com.barbearia.apibarbearia.notification.email.sender.EmailSender;
import br.com.barbearia.apibarbearia.notification.email.template.catalog.*;
import br.com.barbearia.apibarbearia.users.entity.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class CatalogEmailNotificationService {

    private final EmailSender emailSender;
    private final CatalogCreatedTemplate createdTemplate;
    private final CatalogUpdatedTemplate updatedTemplate;
    private final CatalogDeletedTemplate deletedTemplate;
    private final CatalogStatusTemplate statusTemplate;

    public CatalogEmailNotificationService(
            EmailSender emailSender,
            CatalogCreatedTemplate createdTemplate,
            CatalogUpdatedTemplate updatedTemplate,
            CatalogDeletedTemplate deletedTemplate,
            CatalogStatusTemplate statusTemplate
    ) {
        this.emailSender = emailSender;
        this.createdTemplate = createdTemplate;
        this.updatedTemplate = updatedTemplate;
        this.deletedTemplate = deletedTemplate;
        this.statusTemplate = statusTemplate;
    }

    private String formatPrice(java.math.BigDecimal price) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(price);
    }

    // --- MÉTODOS EXISTENTES (Created, Updated Author/Responsible, Deleted, Toggle) ---
    // (Mantenha os que você já tem, vou focar apenas nos NOVOS/ALTERADOS abaixo)

    @Async
    public void sendCreatedToAuthor(User user, CatalogChangedEvent event) {
        String subject = "Sucesso: Serviço criado no catálogo";
        String html = createdTemplate.htmlForAuthor(user.getName(), event.getItemName(), event.getDurationMinutes(), formatPrice(event.getPrice()));
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendCreatedToResponsible(User user, CatalogChangedEvent event) {
        String subject = "Você é responsável por um novo serviço";
        String html = createdTemplate.htmlForResponsible(user.getName(), event.getItemName(), event.getDurationMinutes(), formatPrice(event.getPrice()), event.getAdminName());
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendCreatedToOthers(User user, CatalogChangedEvent event) {
        String subject = "Novidade: Novo serviço no catálogo";
        String html = createdTemplate.htmlForOthers(user.getName(), event.getItemName(), formatPrice(event.getPrice()));
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendUpdatedToAuthor(User user, CatalogChangedEvent event) {
        String subject = "Você atualizou o serviço " + event.getItemName();
        String html = updatedTemplate.htmlForAuthor(user.getName(), event.getItemName());
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendUpdatedToResponsible(User user, CatalogChangedEvent event) {
        String subject = "Atualização em serviço que você atende";
        String html = updatedTemplate.htmlForResponsible(user.getName(), event.getItemName(), event.getAdminName());
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendDeletedToAuthor(User user, CatalogChangedEvent event) {
        String subject = "Você excluiu um serviço";
        String html = deletedTemplate.htmlForAuthor(user.getName(), event.getItemName());
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendDeletedToResponsible(User user, CatalogChangedEvent event) {
        String subject = "Aviso: Serviço removido";
        String html = deletedTemplate.htmlForResponsible(user.getName(), event.getItemName(), event.getAdminName());
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendDeletedToOthers(User user, CatalogChangedEvent event) {
        String subject = "Serviço removido do catálogo";
        String html = deletedTemplate.htmlForOthers(user.getName(), event.getItemName());
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendStatusChanged(User user, CatalogChangedEvent event) {
        boolean activated = (event.getType() == br.com.barbearia.apibarbearia.catalog.events.CatalogEventType.ACTIVATED);
        String subject = activated ? "Serviço Ativado" : "Serviço Desativado";
        String html = statusTemplate.html(user.getName(), event.getItemName(), activated, event.getAdminName());
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    // --- NOVOS MÉTODOS PARA O DIFF ---

    @Async
    public void sendAddedAsResponsible(User user, CatalogChangedEvent event) {
        // Caso: Usuário foi adicionado a um serviço JÁ existente durante um Update.
        // A mensagem é similar a "Você é responsável por um novo serviço".
        String subject = "Você foi incluído como responsável";
        String html = createdTemplate.htmlForResponsible(
                user.getName(),
                event.getItemName(),
                event.getDurationMinutes(),
                formatPrice(event.getPrice()),
                event.getAdminName()
        );
        emailSender.sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendRemovedAsResponsible(User user, CatalogChangedEvent event) {
        // Caso: Usuário deixou de ser responsável durante um Update.
        String subject = "Você foi removido como responsável";
        String html = updatedTemplate.htmlForRemovedResponsible(
                user.getName(),
                event.getItemName(),
                event.getAdminName()
        );
        emailSender.sendHtml(user.getEmail(), subject, html);
    }
}