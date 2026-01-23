package br.com.barbearia.apibarbearia.notification.email.template.users;


import org.springframework.stereotype.Component;

@Component
public class PasswordResetCodeTemplate {

    public String subject() {
        return "Recuperação de Senha - Código de Verificação";
    }

    public String html(String nome, String code) {
        String template =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<body style=\"margin: 0; padding: 20px; background-color: #f4f4f4; font-family: sans-serif;\">" +
                        "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%%\" style=\"background-color: #f4f4f4;\">" +
                        "        <tr>" +
                        "            <td align=\"center\">" +
                        "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%%\" style=\"max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden;\">" +
                        "                    <tr>" +
                        "                        <td style=\"background-color: #2c3e50; padding: 30px; text-align: center;\">" +
                        "                            <h1 style=\"color: #ffffff; margin: 0;\">Barbearia Online</h1>" +
                        "                        </td>" +
                        "                    </tr>" +
                        "                    <tr>" +
                        "                        <td style=\"padding: 40px;\">" +
                        "                            <h2 style=\"color: #333; margin-top: 0;\">Olá, %s!</h2>" +
                        "                            <p style=\"color: #666; font-size: 16px;\">Você solicitou a redefinição da sua senha. Use o código abaixo para continuar:</p>" +
                        "                            " +
                        "                            <div style=\"background-color: #e8f0fe; color: #1a73e8; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; margin: 30px 0; letter-spacing: 5px; border-radius: 8px;\">" +
                        "                                %s" +
                        "                            </div>" +
                        "                            " +
                        "                            <p style=\"color: #999; font-size: 14px;\">Este código expira em 5 minutos.</p>" +
                        "                            <p style=\"color: #999; font-size: 14px;\">Se não foi você, ignore este e-mail.</p>" +
                        "                        </td>" +
                        "                    </tr>" +
                        "                </table>" +
                        "            </td>" +
                        "        </tr>" +
                        "    </table>" +
                        "</body>" +
                        "</html>";

        return String.format(template, nome, code);
    }
}