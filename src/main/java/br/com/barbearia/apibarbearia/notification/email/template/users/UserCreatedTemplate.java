package br.com.barbearia.apibarbearia.notification.email.template.users;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class UserCreatedTemplate {

    private final EmailLayout layout;

    public UserCreatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Bem-vindo à " + layout.appName() + " - Acesso ao Sistema";
    }

    public String html(String nome, String email, String tempPassword) {
        // Título e Subtítulo usando o escape do layout
        String title = "Bem-vindo(a), " + layout.escape(nome) + "! ✂️";
        String subtitle = "Sua conta na " + layout.escape(layout.appName())
                + " foi criada com sucesso. Utilize os dados abaixo para acessar.";

        // Construção do conteúdo central
        String content =
                layout.infoRow("E-MAIL DE LOGIN", layout.escape(email)) +
                        layout.tempPasswordBox(tempPassword) +
                        "<div style='margin-top:20px;'></div>" +
                        layout.warning("⚠️ AÇÃO NECESSÁRIA: Por segurança, o sistema exigirá que você cadastre uma nova senha pessoal logo após o primeiro acesso.");

        // URL de redirecionamento para o login
        String ctaUrl = layout.frontendUrl() + "/login";

        // Retorna o template base unificado
        return layout.baseTemplate(title, subtitle, content, "Acessar Sistema", ctaUrl);
    }
}