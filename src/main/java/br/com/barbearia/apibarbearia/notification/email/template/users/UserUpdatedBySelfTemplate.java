package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class UserUpdatedBySelfTemplate {

    private final EmailLayout layout;

    public UserUpdatedBySelfTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Perfil atualizado";
    }

    public String html(String nome) {
        String title = "Perfil Atualizado ✅";
        String subtitle = "Olá " + layout.escape(nome) + ", confirmamos a alteração dos seus dados.";
        String content = layout.note("Se você realizou essa alteração, nenhuma ação adicional é necessária.");

        String ctaUrl = layout.frontendUrl() + "/perfil";
        return layout.baseTemplate(title, subtitle, content, "Ver Meu Perfil", ctaUrl);
    }
}
