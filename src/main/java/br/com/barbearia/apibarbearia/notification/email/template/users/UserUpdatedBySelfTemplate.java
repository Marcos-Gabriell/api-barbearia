package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

@Component
public class UserUpdatedBySelfTemplate {

    private final EmailLayout layout;

    public UserUpdatedBySelfTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Segurança: Dados atualizados";
    }

    public String html(String nome) {
        String content =
                layout.paragraph("Olá, " + nome + ".") +
                        layout.paragraph("Confirmamos que os dados do seu perfil (Nome ou E-mail) foram atualizados com sucesso.") +
                        layout.warning("Se não foi você, entre em contato com o suporte imediatamente.");

        return layout.baseTemplate(
                "Dados Atualizados",
                "Alteração realizada com sucesso",
                content,
                "Acessar Conta",
                layout.frontendUrl() + "/login"
        );
    }
}