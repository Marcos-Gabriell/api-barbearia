package br.com.barbearia.apibarbearia.notification.email.template.catalog;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class CatalogDeletedTemplate {

    private final EmailLayout layout;

    public CatalogDeletedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String htmlForAuthor(String nome, String serviceName) {
        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Serviço Excluído 🗑️",
                layout.paragraph("Você confirmou a exclusão do serviço <b>" + serviceName + "</b> do sistema."),
                "Ir para o Painel",
                layout.frontendUrl() + "/dashboard"
        );
    }

    public String htmlForResponsible(String nome, String serviceName, String deletedBy) {
        String content = layout.paragraph("O serviço <b>" + serviceName + "</b> foi descontinuado e removido do catálogo.") +
                layout.note("Ação realizada por: " + deletedBy);

        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Serviço Removido ⚠️",
                content,
                "Ver Catálogo",
                layout.frontendUrl() + "/catalogo"
        );
    }

    public String htmlForOthers(String nome, String serviceName) {
        return layout.baseTemplate(
                "Olá, " + layout.escape(nome),
                "Atualização do Catálogo",
                layout.paragraph("O serviço <b>" + serviceName + "</b> não está mais disponível em nossa grade."),
                "Ver Catálogo Atualizado",
                layout.frontendUrl() + "/catalogo"
        );
    }
}