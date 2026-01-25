package br.com.barbearia.apibarbearia.notification.email.template.catalog;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class CatalogCreatedTemplate {

    private final EmailLayout layout;

    public CatalogCreatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String htmlForAuthor(String nome, String serviceName, int duration, String price) {
        String content = layout.paragraph("O serviço foi criado e salvo com sucesso no sistema.") +
                "<br>" +
                layout.infoRow("Serviço", serviceName) +
                layout.infoRow("Preço", price) +
                layout.infoRow("Duração", duration + " min");

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Serviço criado com sucesso! ✅",
                content,
                "Ver no Catálogo",
                layout.frontendUrl() + "/catalogo"
        );
    }

    // Alterado: Recebe Nome e Email
    public String htmlForResponsible(String nome, String serviceName, int duration, String price, String authorName, String authorEmail) {
        String content = layout.paragraph("Você foi incluído como responsável técnico por este novo serviço.") +
                "<br>" +
                layout.infoRow("Serviço", serviceName) +
                layout.infoRow("Preço", price) +
                layout.infoRow("Duração", duration + " min") +
                "<br>" +
                layout.note("Criado por: " + authorName + " (" + authorEmail + ")");

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Novo serviço atribuído a você ✂️",
                content,
                "Ver Detalhes",
                layout.frontendUrl() + "/catalogo"
        );
    }

    public String htmlForOthers(String nome, String serviceName, String price) {
        String content = layout.paragraph("Temos novidade! Um novo serviço acaba de ser adicionado ao nosso catálogo.") +
                "<br>" +
                layout.infoRow("Serviço", serviceName) +
                layout.infoRow("Valor", price);

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Novidade no Catálogo ✨",
                content,
                "Conferir",
                layout.frontendUrl() + "/catalogo"
        );
    }
}