package br.com.barbearia.apibarbearia.notification.email.template.catalog;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class CatalogStatusTemplate {

    private final EmailLayout layout;

    public CatalogStatusTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String html(String nome, String serviceName, boolean activated, String byUser) {
        String statusText = activated ? "ATIVADO (Visível)" : "DESATIVADO (Oculto)";
        String color = activated ? "#10b981" : "#ef4444"; // Verde ou Vermelho

        String content = layout.paragraph("O status do serviço " + serviceName + " foi alterado.") +
                "<br>" +
                "<div style='background-color: " + color + "15; border: 1px solid " + color + "; padding: 10px; border-radius: 8px; color: " + color + "; font-weight: bold; text-align: center;'>" +
                statusText +
                "</div>" +
                "<br>" +
                layout.note("Alterado por: " + byUser);

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Status Alterado 🔄",
                content,
                "Ver Serviço",
                layout.frontendUrl() + "/catalogo"
        );
    }
}