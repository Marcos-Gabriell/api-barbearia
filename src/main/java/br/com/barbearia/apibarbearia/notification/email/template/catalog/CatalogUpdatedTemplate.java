package br.com.barbearia.apibarbearia.notification.email.template.catalog;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class CatalogUpdatedTemplate {

    private final EmailLayout layout;

    public CatalogUpdatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Serviço atualizado no catálogo";
    }

    public String html(String nome, String serviceName, int durationMinutes, String price, String updatedBy) {
        String title = "Serviço atualizado ✨";
        String subtitle = "Olá " + layout.escape(nome) + ", um serviço do catálogo foi atualizado.";

        String content =
                layout.infoRow("SERVIÇO", layout.escape(serviceName)) +
                        layout.infoRow("DURAÇÃO", layout.escape(durationMinutes + " min")) +
                        layout.infoRow("PREÇO", layout.escape("R$ " + price)) +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Atualizado por: " + layout.escape(updatedBy)) +
                        "<div style='margin-top:12px;'></div>" +
                        layout.warning("✅ Se você tiver agendamentos futuros, confira se algo mudou.");

        String ctaUrl = layout.frontendUrl() + "/catalogo";
        return layout.baseTemplate(title, subtitle, content, "Ver Catálogo", ctaUrl);
    }
}
