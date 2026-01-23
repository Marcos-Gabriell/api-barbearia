package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedTemplate {

    private final EmailLayout layout;

    public UserCreatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Bem-vindo à " + layout.appName() + "! ✂️";
    }

    public String html(String nome) {

        String content =
                // Boas-vindas
                layout.paragraph("Sua conta foi configurada e ativada com sucesso! Ficamos muito felizes em ter você na equipe.") +
                        layout.paragraph("Você já pode acessar o sistema utilizando o e-mail e a senha que você acabou de definir.") +

                        // Espaçamento visual (se seu layout suportar tags HTML básicas dentro do parágrafo, senão use parágrafos vazios)
                        "<br>" +

                        // Dicas de Segurança
                        layout.warning("Dicas importantes de Segurança:") +
                        "<ul style=\"color: #6b7280; font-size: 14px; padding-left: 20px; margin-top: 5px;\">" +
                        "<li>Nunca compartilhe sua senha com terceiros;</li>" +
                        "<li>Nossa equipe nunca solicitará sua senha por e-mail ou telefone;</li>" +
                        "<li>Sempre faça <b>Logout</b> ao acessar de computadores compartilhados.</li>" +
                        "</ul>";

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome) + "!",
                "Seja muito bem-vindo(a)!",
                content,
                "Acessar Sistema",
                layout.frontendUrl() + "/login"
        );
    }
}