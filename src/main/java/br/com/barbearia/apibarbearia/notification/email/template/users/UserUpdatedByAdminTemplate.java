package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class UserUpdatedByAdminTemplate {

    private final EmailLayout layout;

    public UserUpdatedByAdminTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Cadastro atualizado";
    }

    public String html(String nome, String adminName, String adminRole) {
        String title = "Cadastro Atualizado 🛡️";
        String subtitle = "Olá " + layout.escape(nome) + ", seu cadastro foi atualizado por um administrador.";

        String content =
                layout.infoRow("Responsável", layout.escape(adminName)) +
                        layout.infoRow("Cargo", layout.escape(adminRole)) +
                        layout.note("Caso não reconheça esta alteração, entre em contato com o suporte.");

        String ctaUrl = layout.frontendUrl() + "/login";
        return layout.baseTemplate(title, subtitle, content, "Acessar Conta", ctaUrl);
    }
}
