package br.com.barbearia.apibarbearia.notification.email.template.catalog;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class CatalogCreatedTemplate {

    private final EmailLayout layout;

    public CatalogCreatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Novo serviço disponível no catálogo";
    }

    public String html(String nome, String serviceName, int durationMinutes, String price, String createdBy) {
        String title = "Novo serviço no catálogo ✅";
        String subtitle = "Olá " + layout.escape(nome) + ", um novo serviço foi adicionado ao catálogo da " + layout.escape(layout.appName()) + ".";

        String content =
                layout.infoRow("SERVIÇO", layout.escape(serviceName)) +
                        layout.infoRow("DURAÇÃO", layout.escape(durationMinutes + " min")) +
                        layout.infoRow("PREÇO", layout.escape("R$ " + price)) +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Adicionado por: " + layout.escape(createdBy)) +
                        "<div style='margin-top:12px;'></div>" +
                        layout.warning("📌 Esse serviço já pode ser usado em novos agendamentos.");

        String ctaUrl = layout.frontendUrl() + "/catalogo"; // ajuste rota do front se precisar
        return layout.baseTemplate(title, subtitle, content, "Ver Catálogo", ctaUrl);
    }
}
