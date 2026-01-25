package br.com.barbearia.apibarbearia.notification.email.template.catalog;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class CatalogUpdatedTemplate {

    private final EmailLayout layout;

    public CatalogUpdatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String htmlForAuthor(String nome, String serviceName) {
        String content = layout.paragraph("As alterações no serviço " + serviceName + " foram salvas com sucesso.");

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Edição Confirmada ✏️",
                content,
                "Ver Alterações",
                layout.frontendUrl() + "/catalogo"
        );
    }

    public String htmlForResponsible(String nome, String serviceName, String authorName, String authorEmail) {
        String content = layout.paragraph("O serviço " + serviceName + ", que você atende, sofreu atualizações recentes.") +
                "<br>" +
                layout.note("Atualizado por: " + authorName + " (" + authorEmail + ")") +
                layout.warning("Por favor, verifique se houve mudança no preço ou tempo de execução.");

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Atualização de Serviço 🔄",
                content,
                "Conferir Mudanças",
                layout.frontendUrl() + "/catalogo"
        );
    }

    public String htmlForRemovedResponsible(String nome, String serviceName, String authorName, String authorEmail) {
        String content = layout.paragraph("Você não é mais listado como responsável técnico pelo serviço " + serviceName + ".") +
                "<br>" +
                layout.note("Alteração realizada por: " + authorName + " (" + authorEmail + ")");

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Vínculo Removido ⚠️",
                content,
                "Ver Catálogo",
                layout.frontendUrl() + "/catalogo"
        );
    }
}