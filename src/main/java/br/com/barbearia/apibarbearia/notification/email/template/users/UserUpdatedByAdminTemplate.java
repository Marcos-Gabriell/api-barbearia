package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

@Component
public class UserUpdatedByAdminTemplate {

    private final EmailLayout layout;

    public UserUpdatedByAdminTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Seus dados de perfil foram alterados";
    }

    public String html(String userName, String userEmail, String userRole, String adminName, String adminRole) {
        String content =
                layout.paragraph("Seus dados cadastrais foram atualizados administrativamente.") +
                        layout.infoRow("RESPONSÁVEL", layout.escape(adminName) + " (" + layout.escape(adminRole) + ")") +
                        "<hr style='border:0; border-top:1px dashed #e2e8f0; margin:15px 0;'>" +
                        layout.paragraph("Como seus dados estão agora:") +
                        layout.infoRow("NOME", layout.escape(userName)) +
                        layout.infoRow("E-MAIL", layout.escape(userEmail)) +
                        layout.infoRow("PERMISSÃO", layout.escape(userRole)) +
                        layout.note("Caso note algum erro, contate o administrador.");

        return layout.baseTemplate(
                "Perfil Atualizado",
                "Confira as alterações realizadas",
                content,
                "Ver Meu Perfil",
                layout.frontendUrl() + "/perfil"
        );
    }
}