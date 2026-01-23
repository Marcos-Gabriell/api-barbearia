package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

@Component
public class PasswordChangedTemplate {

    private final EmailLayout layout;

    public PasswordChangedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Segurança: Senha alterada com sucesso";
    }

    public String html(String nome) {
        String content =
                layout.paragraph("Olá, " + nome + ".") +
                        layout.paragraph("Confirmamos que sua senha foi alterada recentemente.") +
                        layout.paragraph("Se foi você, pode ignorar este e-mail.") +
                        layout.warning("Se NÃO foi você, recupere sua conta agora mesmo.");

        return layout.baseTemplate(
                "Senha Alterada",
                "Aviso de segurança da sua conta",
                content,
                "Recuperar Minha Conta",
                layout.frontendUrl() + "/recuperar-senha"
        );
    }
}