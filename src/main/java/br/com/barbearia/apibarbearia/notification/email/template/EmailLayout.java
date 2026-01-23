package br.com.barbearia.apibarbearia.notification.email.template;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailLayout {

    @Value("${app.name:Barbearia Online}")
    private String appName;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public String frontendUrl() { return frontendUrl; }
    public String appName() { return appName; }

    /**
     * Gera o template base.
     * Se ctaUrl for NULL, o botão não será renderizado.
     */
    public String baseTemplate(String title, String subtitle, String contentHtml, String ctaText, String ctaUrl) {

        // Lógica do Botão Opcional
        String btnHtml = "";
        if (ctaUrl != null && !ctaUrl.isEmpty()) {
            btnHtml = "<div class='btn-container'>"
                    + "  <a href='" + escapeAttr(ctaUrl) + "' class='btn'>" + escape(ctaText) + "</a>"
                    + "</div>";
        }

        return "<!doctype html>"
                + "<html lang='pt-BR'>"
                + "<head>"
                + "  <meta charset='utf-8'/>"
                + "  <meta name='viewport' content='width=device-width, initial-scale=1'/>"
                + "  <style>"
                /* Reset e Base */
                + "    body{margin:0;padding:0;background-color:#ffffff;font-family:'Segoe UI', Arial, sans-serif;-webkit-font-smoothing:antialiased;}"
                + "    .wrapper{width:100%;background-color:#ffffff;padding:0;}"
                + "    .main-card{max-width:500px;margin:20px auto;background-color:#ffffff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;}"
                /* Conteúdo */
                + "    .content{padding:24px;text-align:center;color:#334155;}"
                + "    .content h1{color:#1e293b;font-size:22px;margin:0 0 10px 0;font-weight:700;}"
                + "    .content p{font-size:15px;line-height:1.5;color:#64748b;margin-bottom:20px;}"
                /* Data Box (Caixa Cinza Interna) */
                + "    .data-box{background-color:#ffffff;border:1px solid #f1f5f9;border-radius:12px;padding:20px;margin:20px 0;text-align:left;}"
                /* Linhas de Informação */
                + "    .label{font-size:11px;font-weight:700;color:#94a3b8;letter-spacing:1px;margin-bottom:4px;text-transform:uppercase;}"
                + "    .value{font-size:15px;font-weight:600;color:#0f172a;margin-bottom:15px;word-break:break-all;}"
                /* Botão */
                + "    .btn-container{margin:20px 0;text-align:center;}"
                + "    .btn{background-color:#2563eb;color:#ffffff !important;padding:14px 35px;text-decoration:none;border-radius:8px;font-weight:600;display:inline-block;font-size:15px;}"
                /* Alertas e Caixas Especiais */
                + "    .warning-box{background-color:#fffbeb;border:1px solid #fef3c7;padding:15px;color:#92400e;font-size:13px;border-radius:8px;line-height:1.5;text-align:left; margin-top: 15px;}"
                + "    .code-box{background:#f8fafc; padding:15px; border:1px dashed #cbd5e1; border-radius:8px; font-family:monospace; font-size:24px; color:#1e293b; text-align:center; margin:10px 0; font-weight:bold; letter-spacing: 3px;}"
                /* Footer */
                + "    .footer{padding:16px;color:#94a3b8;font-size:11px;line-height:1.4;text-align:center;border-top:1px solid #e5e7eb;}"
                + "  </style>"
                + "</head>"
                + "<body>"
                + "  <div style='display:none;max-height:0;overflow:hidden;'>Notificação " + escape(appName) + "</div>"
                + "  <div class='wrapper'>"
                + "    <div class='main-card'>"
                + "      <div class='content'>"
                + "        <h1>" + title + "</h1>"
                + "        <p>" + subtitle + "</p>"
                /* Todo o conteúdo específico entra aqui dentro da Data-Box */
                + "        <div class='data-box'>" + contentHtml + "</div>"
                +          btnHtml
                + "      </div>"
                + "      <div class='footer'>"
                + "        Mensagem automática enviada por " + escape(appName) + ".<br/>"
                + "        © 2026 - Todos os direitos reservados."
                + "      </div>"
                + "    </div>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    // --- Helpers Visuais ---

    public String infoRow(String label, String valueHtml) {
        return "<div style='margin-bottom:12px;'>"
                + "  <div class='label'>" + escape(label) + "</div>"
                + "  <div class='value'>" + valueHtml + "</div>"
                + "</div>";
    }

    public String paragraph(String text) {
        return "<p style='margin:0 0 10px 0; color:#334155; font-size:14px;'>" + escape(text) + "</p>";
    }

    public String warning(String text) {
        return "<div class='warning-box'><strong>⚠️ Atenção:</strong> " + escape(text) + "</div>";
    }

    // ADICIONEI ESTE MÉTODO QUE ESTAVA FALTANDO
    public String note(String text) {
        return "<p style='font-size:12px; color:#94a3b8; font-style:italic; margin-top:15px; margin-bottom:0;'>"
                + escape(text) + "</p>";
    }

    // Para Senhas ou Tokens curtos
    public String tempPasswordBox(String label, String code) {
        return infoRow(label, "<div class='code-box' style='font-size: 20px;'>" + escape(code) + "</div>");
    }

    // Para Códigos de Verificação (Maior destaque)
    public String codeBox(String code) {
        return "<div class='code-box'>" + escape(code) + "</div>";
    }

    // --- Segurança ---

    public String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public String escapeAttr(String s) {
        return escape(s);
    }
}