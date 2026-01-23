package br.com.barbearia.apibarbearia.notification.email.users;

import br.com.barbearia.apibarbearia.notification.email.sender.EmailSender;
import br.com.barbearia.apibarbearia.notification.email.template.users.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class UserEmailNotificationService {

    private final EmailSender emailSender;

    private final UserCreatedTemplate userCreatedTemplate;
    private final UserUpdatedBySelfTemplate updatedBySelfTemplate;
    private final UserUpdatedByAdminTemplate updatedByAdminTemplate;
    private final PasswordChangedTemplate passwordChangedTemplate;
    private final UserPasswordResetByAdminTemplate resetByAdminTemplate;
    private final UserDeletedByAdminTemplate deletedByAdminTemplate;
    private final EmailVerificationCodeTemplate verificationCodeTemplate;
    private final PasswordResetCodeTemplate passwordResetCodeTemplate;
    private final InviteUserTemplate inviteUserTemplate;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public UserEmailNotificationService(
            EmailSender emailSender,
            UserCreatedTemplate userCreatedTemplate,
            UserUpdatedBySelfTemplate updatedBySelfTemplate,
            UserUpdatedByAdminTemplate updatedByAdminTemplate,
            PasswordChangedTemplate passwordChangedTemplate,
            UserPasswordResetByAdminTemplate resetByAdminTemplate,
            UserDeletedByAdminTemplate deletedByAdminTemplate,
            EmailVerificationCodeTemplate verificationCodeTemplate,
            PasswordResetCodeTemplate passwordResetCodeTemplate,
            InviteUserTemplate inviteUserTemplate
    ) {
        this.emailSender = emailSender;
        this.userCreatedTemplate = userCreatedTemplate;
        this.updatedBySelfTemplate = updatedBySelfTemplate;
        this.updatedByAdminTemplate = updatedByAdminTemplate;
        this.passwordChangedTemplate = passwordChangedTemplate;
        this.resetByAdminTemplate = resetByAdminTemplate;
        this.deletedByAdminTemplate = deletedByAdminTemplate;
        this.verificationCodeTemplate = verificationCodeTemplate;
        this.passwordResetCodeTemplate = passwordResetCodeTemplate;
        this.inviteUserTemplate = inviteUserTemplate;
    }

    @Async
    public void sendInvite(String to, String token) {
        String link = frontendUrl + "/setup-conta?token=" + token;

        emailSender.sendHtml(
                to,
                inviteUserTemplate.subject(),
                inviteUserTemplate.html(link)
        );
    }

    // =========================================================================
    // ✅ CORREÇÃO AQUI: Assinatura atualizada para receber apenas (to, nome)
    // =========================================================================
    @Async
    public void sendUserCreated(String to, String nome) {
        // O template userCreatedTemplate.html(nome) também já deve ter sido
        // ajustado para receber apenas o nome, conforme fizemos anteriormente.
        emailSender.sendHtml(to, userCreatedTemplate.subject(), userCreatedTemplate.html(nome));
    }

    @Async
    public void sendUserUpdatedBySelf(String to, String nome) {
        emailSender.sendHtml(to, updatedBySelfTemplate.subject(), updatedBySelfTemplate.html(nome));
    }

    @Async
    public void sendUserUpdatedByAdmin(String to, String nome, String userEmail, String userRole, String adminName, String adminRole) {
        emailSender.sendHtml(to, updatedByAdminTemplate.subject(), updatedByAdminTemplate.html(nome, userEmail, userRole, adminName, adminRole));
    }

    @Async
    public void sendPasswordChanged(String to, String nome) {
        emailSender.sendHtml(to, passwordChangedTemplate.subject(), passwordChangedTemplate.html(nome));
    }

    @Async
    public void sendPasswordResetByAdmin(String to, String nome, String tempPassword, String adminName) {
        emailSender.sendHtml(to, resetByAdminTemplate.subject(), resetByAdminTemplate.html(nome, tempPassword, adminName));
    }

    @Async
    public void sendUserDeletedByAdmin(String to, String nome, String adminName) {
        emailSender.sendHtml(to, deletedByAdminTemplate.subject(), deletedByAdminTemplate.html(nome, adminName));
    }

    @Async
    public void sendVerificationCode(String to, String nome, String code) {
        emailSender.sendHtml(to, verificationCodeTemplate.subject(), verificationCodeTemplate.html(nome, code));
    }

    @Async
    public void sendPasswordResetCode(String to, String nome, String code) {
        emailSender.sendHtml(to, passwordResetCodeTemplate.subject(), passwordResetCodeTemplate.html(nome, code));
    }
}