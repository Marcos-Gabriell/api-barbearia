package br.com.barbearia.apibarbearia.notification.email.template.appointment;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Template de CONFIRMAÇÃO DE AGENDAMENTO — enviado ao cliente.
 *
 * ══ TEMA ══
 * LIGHT MODE FORÇADO — independente do sistema/preferência do cliente.
 * Técnicas aplicadas:
 *   • <meta name="color-scheme" content="light">
 *   • <meta name="supported-color-schemes" content="light">
 *   • <style> :root { color-scheme: light only } </style>
 *   • bgcolor="..." em todos os containers (legado Outlook)
 *   • background-color inline em todos os divs principais
 *   • Fonte Arial/Helvetica em vez de fonts do sistema
 *   • Comentários condicionais <!--[if mso]> para Outlook
 *
 * ══ ASSINATURA ══
 * Idêntica ao original — zero breaking change.
 */
@Component
public class AppointmentCreatedForClientTemplate {

    private final EmailLayout layout;

    public AppointmentCreatedForClientTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "✅ Agendamento confirmado — " + layout.appName();
    }

    /** Assinatura IDÊNTICA ao original */
    public String html(String clientName, String professionalName,
                       String serviceName, LocalDateTime startAt, String cancelLink) {

        String dayFmt   = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String timeFmt  = startAt.format(DateTimeFormatter.ofPattern("HH:mm"));
        String fullDate = startAt.format(
                DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR")));

        return wrapper(
                hero(layout.escape(clientName)) +
                        card(layout.escape(serviceName), layout.escape(professionalName), dayFmt, timeFmt, fullDate) +
                        tips() +
                        banner() +
                        cancelBtn(cancelLink)
        );
    }

    // ── Seções ────────────────────────────────────────────────────────────────

    private String hero(String clientName) {
        return
                "<div bgcolor='#10b981' style='background-color:#10b981;"
                        + "padding:32px 32px 24px;text-align:center;"
                        + "border-radius:12px 12px 0 0;'>"
                        + "  <div style='display:inline-block;background:rgba(255,255,255,0.25);"
                        + "       border:2px solid rgba(255,255,255,0.6);border-radius:50%;"
                        + "       width:52px;height:52px;line-height:52px;font-size:22px;"
                        + "       margin-bottom:14px;color:#ffffff;'>✓</div>"
                        + "  <h1 style='margin:0 0 8px;font-size:22px;font-weight:800;color:#ffffff;"
                        + "       letter-spacing:-0.3px;font-family:Arial,Helvetica,sans-serif;'>"
                        + "       Agendamento Confirmado!</h1>"
                        + "  <p style='margin:0;font-size:14px;color:rgba(255,255,255,0.9);"
                        + "       font-family:Arial,Helvetica,sans-serif;'>"
                        + "       Olá, <strong>" + clientName + "</strong> — seu horário está garantido.</p>"
                        + "</div>";
    }

    private String card(String service, String professional,
                        String day, String time, String fullDate) {
        return
                "<div bgcolor='#ffffff' style='background-color:#ffffff;padding:24px 32px;"
                        + "border-left:4px solid #10b981;border-bottom:1px solid #f0f0f0;'>"
                        + "  <p style='margin:0 0 14px;font-size:10px;font-weight:700;letter-spacing:1.5px;"
                        + "       color:#10b981;text-transform:uppercase;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>Detalhes do Agendamento</p>"
                        + row("🪒", "Serviço",      service)
                        + row("👤", "Profissional", professional)
                        + row("📅", "Data",         fullDate)
                        // Destaque do horário
                        + "  <div bgcolor='#f0fdf4' style='background-color:#f0fdf4;border:1px solid #bbf7d0;"
                        + "       border-radius:10px;padding:16px;margin-top:18px;text-align:center;'>"
                        + "    <p style='margin:0 0 2px;font-size:10px;color:#6b7280;"
                        + "         text-transform:uppercase;letter-spacing:1px;"
                        + "         font-family:Arial,Helvetica,sans-serif;'>Horário Agendado</p>"
                        + "    <p style='margin:0;font-size:34px;font-weight:800;color:#065f46;"
                        + "         letter-spacing:-1px;font-family:Arial,Helvetica,sans-serif;'>" + time + "</p>"
                        + "    <p style='margin:4px 0 0;font-size:12px;color:#6b7280;"
                        + "         font-family:Arial,Helvetica,sans-serif;'>" + day + "</p>"
                        + "  </div>"
                        + "</div>";
    }

    private String row(String icon, String label, String value) {
        return
                "<div style='display:flex;align-items:center;gap:12px;padding:8px 0;"
                        + "border-bottom:1px solid #f3f4f6;'>"
                        + "  <span style='font-size:14px;width:20px;text-align:center;'>" + icon + "</span>"
                        + "  <div>"
                        + "    <p style='margin:0;font-size:10px;color:#9ca3af;text-transform:uppercase;"
                        + "         letter-spacing:0.8px;font-family:Arial,Helvetica,sans-serif;'>" + label + "</p>"
                        + "    <p style='margin:2px 0 0;font-size:13px;color:#111827;font-weight:500;"
                        + "         font-family:Arial,Helvetica,sans-serif;'>" + value + "</p>"
                        + "  </div>"
                        + "</div>";
    }

    private String tips() {
        return
                "<div bgcolor='#f9fafb' style='background-color:#f9fafb;padding:18px 32px;"
                        + "border-bottom:1px solid #f0f0f0;'>"
                        + "  <p style='margin:0 0 10px;font-size:10px;font-weight:700;letter-spacing:1px;"
                        + "       color:#6b7280;text-transform:uppercase;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>Informações Importantes</p>"
                        + tip("🕐", "Chegue com <strong>5 minutos de antecedência</strong> para garantir seu atendimento.")
                        + tip("📎", "O comprovante PDF está <strong>em anexo</strong> neste e-mail — guarde para referência.")
                        + tip("❌", "Cancelamentos devem ser feitos com pelo menos <strong>10 min de antecedência</strong>.")
                        + "</div>";
    }

    private String tip(String icon, String html) {
        return
                "<div style='display:flex;align-items:flex-start;gap:10px;margin-bottom:8px;'>"
                        + "  <span style='font-size:13px;flex-shrink:0;'>" + icon + "</span>"
                        + "  <p style='margin:0;font-size:12px;color:#4b5563;line-height:1.55;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>" + html + "</p>"
                        + "</div>";
    }

    private String banner() {
        return
                "<div bgcolor='#065f46' style='background-color:#065f46;"
                        + "background:linear-gradient(135deg,#065f46 0%,#10b981 100%);"
                        + "padding:22px 32px;text-align:center;'>"
                        + "  <p style='margin:0 0 4px;font-size:17px;font-weight:800;color:#ffffff;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>Aguardamos você! 💈</p>"
                        + "  <p style='margin:0;font-size:12px;color:rgba(255,255,255,0.85);"
                        + "       font-family:Arial,Helvetica,sans-serif;'>"
                        + "       Nosso time está pronto para a melhor experiência em "
                        + layout.appName() + ".</p>"
                        + "</div>";
    }

    private String cancelBtn(String cancelLink) {
        if (cancelLink == null || cancelLink.isBlank()) return "";
        return
                "<div bgcolor='#ffffff' style='background-color:#ffffff;padding:14px 32px;"
                        + "text-align:center;border-top:1px solid #f3f4f6;'>"
                        + "  <p style='margin:0 0 8px;font-size:11px;color:#9ca3af;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>"
                        + "       Precisa cancelar? Use o link abaixo com pelo menos 10 minutos de antecedência:</p>"
                        + "  <a href='" + cancelLink + "' "
                        + "     style='display:inline-block;padding:8px 22px;background-color:#ffffff;"
                        + "     color:#ef4444;border:1.5px solid #ef4444;border-radius:50px;"
                        + "     font-size:12px;font-weight:600;text-decoration:none;"
                        + "     font-family:Arial,Helvetica,sans-serif;'>Cancelar Agendamento</a>"
                        + "</div>";
    }

    // ── Wrapper ───────────────────────────────────────────────────────────────

    private String wrapper(String body) {
        return
                "<!DOCTYPE html>"
                        + "<html lang='pt-BR' xmlns='http://www.w3.org/1999/xhtml'>"
                        + "<head>"
                        + "  <meta charset='UTF-8'>"
                        + "  <meta name='viewport' content='width=device-width,initial-scale=1.0'>"
                        // Force light mode em clientes modernos (Apple Mail, Gmail app etc.)
                        + "  <meta name='color-scheme' content='light'>"
                        + "  <meta name='supported-color-schemes' content='light'>"
                        + "  <title>Agendamento Confirmado</title>"
                        + "  <style>"
                        // Impede que qualquer cliente inverta as cores
                        + "    :root { color-scheme: light only; }"
                        + "    body  { background-color: #f3f4f6 !important; }"
                        // Outlook dark-mode guard
                        + "    [data-ogsc] body { background-color: #f3f4f6 !important; }"
                        + "  </style>"
                        + "</head>"
                        // bgcolor legado para clientes antigos / Outlook
                        + "<body bgcolor='#f3f4f6' "
                        + "style='margin:0;padding:0;background-color:#f3f4f6;"
                        + "font-family:Arial,Helvetica,sans-serif;-webkit-text-size-adjust:100%;'>"
                        // Wrapper para Outlook (não entende max-width em divs)
                        + "<!--[if mso]>"
                        + "<table width='560' align='center' cellpadding='0' cellspacing='0' border='0'><tr><td>"
                        + "<![endif]-->"
                        + "<div style='max-width:560px;margin:28px auto;border-radius:12px;"
                        + "overflow:hidden;box-shadow:0 2px 16px rgba(0,0,0,0.08);"
                        + "border:1px solid #e5e7eb;background-color:#ffffff;'>"
                        + body
                        + footer()
                        + "</div>"
                        + "<!--[if mso]></td></tr></table><![endif]-->"
                        + "</body></html>";
    }

    private String footer() {
        return
                "<div bgcolor='#f9fafb' style='background-color:#f9fafb;padding:12px 32px;"
                        + "text-align:center;border-top:1px solid #f0f0f0;'>"
                        + "  <p style='margin:0;font-size:10px;color:#9ca3af;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>"
                        + "       © " + layout.appName()
                        + " &nbsp;·&nbsp; Este é um e-mail automático, não responda.</p>"
                        + "</div>";
    }
}