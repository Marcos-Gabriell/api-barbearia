package br.com.barbearia.apibarbearia.notification.email.template.users;

import org.springframework.stereotype.Component;

@Component
public class InviteUserTemplate {

    public String subject() {
        return "Você foi convidado para a equipe - Barbearia Online";
    }

    public String html(String link) {
        String htmlTemplate =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "    <style>" +
                        "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; margin: 0; padding: 0; }" +
                        "        .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }" +
                        "        .header { background-color: #0f172a; padding: 30px; text-align: center; }" +
                        "        .header h1 { color: #ffffff; margin: 0; font-size: 24px; letter-spacing: 1px; }" +
                        "        .content { padding: 40px 30px; color: #334155; line-height: 1.6; }" +
                        "        .btn-container { text-align: center; margin: 30px 0; }" +
                        "        .btn { background-color: #10b981; color: #ffffff !important; padding: 14px 28px; text-decoration: none; border-radius: 50px; font-weight: bold; font-size: 16px; display: inline-block; transition: background 0.3s; }" +
                        "        .btn:hover { background-color: #059669; }" +
                        "        .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }" +
                        "        .expiration { background-color: #fff7ed; color: #c2410c; padding: 10px; border-radius: 6px; font-size: 13px; text-align: center; margin-top: 20px; border: 1px solid #ffedd5; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='container'>" +
                        "        <div class='header'>" +
                        "            <h1>Barbearia Online</h1>" +
                        "        </div>" +
                        "        <div class='content'>" +
                        "            <h2>Olá!</h2>" +
                        "            <p>Você recebeu um convite oficial para fazer parte da equipe de gestão da <strong>Barbearia Online</strong>.</p>" +
                        "            <p>Para concluir seu cadastro, definir sua senha e acessar o painel, clique no botão abaixo:</p>" +
                        "            " +
                        "            <div class='btn-container'>" +
                        "                <a href='%s' class='btn'>Aceitar Convite e Criar Conta</a>" +
                        "            </div>" +
                        "" +
                        "            <div class='expiration'>" +
                        "                ⚠️ Este link é válido por apenas <strong>24 horas</strong>." +
                        "            </div>" +
                        "            " +
                        "            <p style='font-size: 13px; margin-top: 30px;'>" +
                        "                Se você não esperava por este convite, pode ignorar este e-mail com segurança." +
                        "            </p>" +
                        "        </div>" +
                        "        <div class='footer'>" +
                        "            <p>&copy; 2026 Barbearia Online. Todos os direitos reservados.</p>" +
                        "            <p>Este é um e-mail automático, por favor não responda.</p>" +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";

        return String.format(htmlTemplate, link);
    }
}