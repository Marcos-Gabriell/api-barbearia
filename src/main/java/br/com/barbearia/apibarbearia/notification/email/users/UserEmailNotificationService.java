package br.com.barbearia.apibarbearia.notification.email.users;

import br.com.barbearia.apibarbearia.notification.email.sender.EmailSender;
import br.com.barbearia.apibarbearia.notification.email.template.users.*;
import org.springframework.stereotype.Service;

@Service
public class UserEmailNotificationService {

    private final EmailSender emailSender;

    private final UserCreatedTemplate userCreatedTemplate;
    private final UserUpdatedBySelfTemplate updatedBySelfTemplate;
    private final UserUpdatedByAdminTemplate updatedByAdminTemplate;
    private final PasswordChangedTemplate passwordChangedTemplate;

    public UserEmailNotificationService(
            EmailSender emailSender,
            UserCreatedTemplate userCreatedTemplate,
            UserUpdatedBySelfTemplate updatedBySelfTemplate,
            UserUpdatedByAdminTemplate updatedByAdminTemplate,
            PasswordChangedTemplate passwordChangedTemplate
    ) {
        this.emailSender = emailSender;
        this.userCreatedTemplate = userCreatedTemplate;
        this.updatedBySelfTemplate = updatedBySelfTemplate;
        this.updatedByAdminTemplate = updatedByAdminTemplate;
        this.passwordChangedTemplate = passwordChangedTemplate;
    }

    public void sendUserCreated(String to, String nome, String email, String tempPassword) {
        emailSender.sendHtml(to, userCreatedTemplate.subject(), userCreatedTemplate.html(nome, email, tempPassword));
    }

    public void sendUserUpdatedBySelf(String to, String nome) {
        emailSender.sendHtml(to, updatedBySelfTemplate.subject(), updatedBySelfTemplate.html(nome));
    }

    public void sendUserUpdatedByAdmin(String to, String nome, String adminName, String adminRole) {
        emailSender.sendHtml(to, updatedByAdminTemplate.subject(), updatedByAdminTemplate.html(nome, adminName, adminRole));
    }

    public void sendPasswordChanged(String to, String nome) {
        emailSender.sendHtml(to, passwordChangedTemplate.subject(), passwordChangedTemplate.html(nome));
    }
}
