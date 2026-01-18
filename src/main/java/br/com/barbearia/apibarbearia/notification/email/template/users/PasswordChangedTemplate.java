package br.com.barbearia.apibarbearia.notification.email.template.users;


import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class PasswordChangedTemplate {

    private final EmailLayout layout;

    public PasswordChangedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Senha alterada com sucesso";
    }

    public String html(String nome) {
        String title = "Senha alterada ✅";
        String subtitle = "Olá " + layout.escape(nome) + ", sua senha foi atualizada com sucesso.";

        String content =
                layout.note("Se você realizou essa alteração, nenhuma ação adicional é necessária.") +
                        "<div style='margin-top:12px;'></div>" +
                        layout.warning("🔐 Dica de segurança: se você não reconhece essa alteração, entre em contato com o suporte imediatamente.");

        String ctaUrl = layout.frontendUrl() + "/login";
        return layout.baseTemplate(title, subtitle, content, "Acessar o sistema", ctaUrl);
    }
}
