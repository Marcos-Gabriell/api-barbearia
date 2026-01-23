package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

@Component
public class UserDeletedByAdminTemplate {

    private final EmailLayout layout;

    public UserDeletedByAdminTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Aviso: Encerramento de conta";
    }

    public String html(String userName, String adminName) {
        String content =
                layout.paragraph("Informamos que sua conta de acesso foi permanentemente encerrada.") +
                        layout.infoRow("USUÁRIO", layout.escape(userName)) +
                        layout.infoRow("RESPONSÁVEL PELA AÇÃO", layout.escape(adminName)) +
                        layout.warning("Você perdeu o acesso ao sistema imediatamente.");
        return layout.baseTemplate(
                "Conta Excluída",
                "Comunicado importante sobre seu acesso",
                content,
                null, null
        );
    }
}