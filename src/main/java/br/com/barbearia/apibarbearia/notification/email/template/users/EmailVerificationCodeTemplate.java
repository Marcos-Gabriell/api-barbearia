package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationCodeTemplate {

    private final EmailLayout layout;

    public EmailVerificationCodeTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Seu código de verificação - " + layout.appName();
    }

    public String html(String nome, String codigo) {
        String content =
                layout.paragraph("Olá, " + nome + ".") +
                        layout.paragraph("Recebemos um pedido de verificação para sua conta.") +
                        layout.infoRow("SEU CÓDIGO", layout.codeBox(codigo)) +
                        layout.warning("Este código expira em 5 minutos. Não compartilhe com ninguém.");

        return layout.baseTemplate(
                "Verifique seu E-mail",
                "Use o código abaixo para continuar",
                content,
                null, null
        );
    }
}