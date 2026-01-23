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

    public String html(String nome, String serviceName, String activatedBy) {
        String title = "Serviço ativado ✅";
        String subtitle = "Olá " + layout.escape(nome) + ", um serviço foi ativado e voltou a ficar disponível.";

        String content =
                layout.infoRow("SERVIÇO", layout.escape(serviceName)) +
                        layout.infoRow("STATUS", layout.escape("ATIVO")) +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Ativado por: " + layout.escape(activatedBy));

        String ctaUrl = layout.frontendUrl() + "/catalogo";
        return layout.baseTemplate(title, subtitle, content, "Ver Catálogo", ctaUrl);
    }
}
