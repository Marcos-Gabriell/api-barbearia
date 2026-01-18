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

    public String baseTemplate(String title, String subtitle, String contentHtml, String ctaText, String ctaUrl) {
        return "<!doctype html>"
                + "<html lang='pt-BR'>"
                + "<head>"
                + "  <meta charset='utf-8'/>"
                + "  <meta name='viewport' content='width=device-width, initial-scale=1'/>"
                + "  <style>"
                + "    body{margin:0;padding:0;background-color:#ffffff;font-family:'Segoe UI', Arial, sans-serif;-webkit-font-smoothing:antialiased;}"
                + "    .wrapper{width:100%;background-color:#ffffff;padding:0;}"
                + "    .main-card{max-width:500px;margin:20px auto;background-color:#ffffff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;}"
                + "    .content{padding:24px;text-align:center;color:#334155;}"
                + "    .content h1{color:#1e293b;font-size:22px;margin:0 0 10px 0;font-weight:700;}"
                + "    .content p{font-size:15px;line-height:1.5;color:#64748b;margin-bottom:20px;}"
                + "    .data-box{background-color:#ffffff;border:1px solid #f1f5f9;border-radius:12px;padding:20px;margin:20px 0;text-align:left;}"
                + "    .label{font-size:11px;font-weight:700;color:#94a3b8;letter-spacing:1px;margin-bottom:4px;}"
                + "    .value{font-size:15px;font-weight:600;color:#0f172a;margin-bottom:15px;word-break:break-all;}"
                + "    .btn-container{margin:20px 0;text-align:center;}"
                + "    .btn{background-color:#2563eb;color:#ffffff !important;padding:14px 35px;text-decoration:none;border-radius:8px;font-weight:600;display:inline-block;font-size:15px;}"
                + "    .warning-box{background-color:#fffbeb;border:1px solid #fef3c7;padding:15px;color:#92400e;font-size:13px;border-radius:8px;line-height:1.5;text-align:left;}"
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
                + "        <div class='data-box'>" + contentHtml + "</div>"
                + "        <div class='btn-container'>"
                + "          <a href='" + escapeAttr(ctaUrl) + "' class='btn'>" + escape(ctaText) + "</a>"
                + "        </div>"
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

    public String infoRow(String label, String valueHtml) {
        return "<div style='margin-bottom:10px;'>"
                + "  <div class='label'>" + escape(label) + "</div>"
                + "  <div class='value'>" + valueHtml + "</div>"
                + "</div>";
    }

    public String warning(String text) {
        return "<div class='warning-box'>" + escape(text) + "</div>";
    }

    public String note(String text) {
        return "<p style='font-size:13px; color:#64748b; font-style:italic; margin-top:10px; text-align:left;'>"
                + escape(text) + "</p>";
    }

    public String tempPasswordBox(String tempPassword) {
        return infoRow("SENHA TEMPORÁRIA",
                "<div style='background:#f8fafc; padding:12px; border:1px solid #e2e8f0; border-radius:8px; font-family:monospace; font-size:20px; color:#1e293b; text-align:center; margin-top:5px; font-weight:bold;'>"
                        + escape(tempPassword) +
                        "</div>");
    }

    public String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;")
                .replace("'","&#39;");
    }

    public String escapeAttr(String s) {
        return escape(s);
    }
}
