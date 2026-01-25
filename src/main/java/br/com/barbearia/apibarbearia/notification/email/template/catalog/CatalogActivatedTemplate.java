package br.com.barbearia.apibarbearia.notification.email.template.catalog;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class CatalogActivatedTemplate {

    private final EmailLayout layout;

    public CatalogActivatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Serviço ativado no catálogo";
    }

    public String html(String nome, String serviceName, String authorName, String authorEmail) {
        String title = "Serviço ativado ✅";
        String subtitle = "Olá " + layout.escape(nome) + ", um serviço foi ativado e voltou a ficar disponível.";

        String content =
                layout.infoRow("SERVIÇO", serviceName) + // Removido escape manual redundante se o layout já trata
                        layout.infoRow("STATUS", "ATIVO") +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Ativado por: " + authorName + " (" + authorEmail + ")");

        String ctaUrl = layout.frontendUrl() + "/catalogo";
        return layout.baseTemplate(title, subtitle, content, "Ver Catálogo", ctaUrl);
    }
}