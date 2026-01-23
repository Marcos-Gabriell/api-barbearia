package br.com.barbearia.apibarbearia.notification.email.catalog;

import br.com.barbearia.apibarbearia.notification.email.sender.EmailSender;
import br.com.barbearia.apibarbearia.notification.email.template.catalog.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CatalogEmailNotificationService {

    private final EmailSender emailSender;

    private final CatalogCreatedTemplate createdTemplate;
    private final CatalogUpdatedTemplate updatedTemplate;
    private final CatalogActivatedTemplate activatedTemplate;
    private final CatalogDeactivatedTemplate deactivatedTemplate;

    public CatalogEmailNotificationService(
            EmailSender emailSender,
            CatalogCreatedTemplate createdTemplate,
            CatalogUpdatedTemplate updatedTemplate,
            CatalogActivatedTemplate activatedTemplate,
            CatalogDeactivatedTemplate deactivatedTemplate
    ) {
        this.emailSender = emailSender;
        this.createdTemplate = createdTemplate;
        this.updatedTemplate = updatedTemplate;
        this.activatedTemplate = activatedTemplate;
        this.deactivatedTemplate = deactivatedTemplate;
    }

    @Async
    public void sendCatalogCreated(String to, String nome, String serviceName, int durationMinutes, String price, String createdBy) {
        emailSender.sendHtml(to, createdTemplate.subject(), createdTemplate.html(nome, serviceName, durationMinutes, price, createdBy));
    }

    @Async
    public void sendCatalogUpdated(String to, String nome, String serviceName, int durationMinutes, String price, String updatedBy) {
        emailSender.sendHtml(to, updatedTemplate.subject(), updatedTemplate.html(nome, serviceName, durationMinutes, price, updatedBy));
    }

    @Async
    public void sendCatalogActivated(String to, String nome, String serviceName, String activatedBy) {
        emailSender.sendHtml(to, activatedTemplate.subject(), activatedTemplate.html(nome, serviceName, activatedBy));
    }

    @Async
    public void sendCatalogDeactivated(String to, String nome, String serviceName, String deactivatedBy) {
        emailSender.sendHtml(to, deactivatedTemplate.subject(), deactivatedTemplate.html(nome, serviceName, deactivatedBy));
    }
}
