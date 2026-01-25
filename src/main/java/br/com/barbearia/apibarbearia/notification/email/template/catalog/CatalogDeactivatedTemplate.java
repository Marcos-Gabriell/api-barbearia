package br.com.barbearia.apibarbearia.notification.email.template.catalog;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class CatalogDeactivatedTemplate {

    private final EmailLayout layout;

    public CatalogDeactivatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Serviço desativado no catálogo";
    }

    public String html(String nome, String serviceName, String authorName, String authorEmail) {
        String title = "Serviço desativado ⚠️";
        String subtitle = "Olá " + layout.escape(nome) + ", um serviço foi desativado e não deve aparecer para novos agendamentos.";

        String content =
                layout.infoRow("SERVIÇO", serviceName) +
                        layout.infoRow("STATUS", "INATIVO") +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Desativado por: " + authorName + " (" + authorEmail + ")") +
                        "<div style='margin-top:12px;'></div>" +
                        layout.warning("⚠️ Se existir agendamento futuro com esse serviço, revise com a equipe.");

        String ctaUrl = layout.frontendUrl() + "/catalogo";
        return layout.baseTemplate(title, subtitle, content, "Ver Catálogo", ctaUrl);
    }
}