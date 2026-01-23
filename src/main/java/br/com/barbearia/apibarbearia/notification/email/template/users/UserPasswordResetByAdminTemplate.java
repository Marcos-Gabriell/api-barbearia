package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordResetByAdminTemplate {

    private final EmailLayout layout;

    public UserPasswordResetByAdminTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Sua senha foi redefinida";
    }

    public String html(String userName, String tempPassword, String adminName) {
        String content =
                layout.paragraph("Sua senha foi redefinida administrativamente.") +
                        layout.infoRow("SOLICITADO POR", layout.escape(adminName)) +
                        layout.tempPasswordBox("NOVA SENHA TEMPORÁRIA", tempPassword) +
                        layout.warning("Crie uma nova senha pessoal assim que entrar.");

        return layout.baseTemplate(
                "Redefinição de Senha",
                "Use a senha abaixo para acessar",
                content,
                "Fazer Login",
                layout.frontendUrl() + "/login"
        );
    }
}