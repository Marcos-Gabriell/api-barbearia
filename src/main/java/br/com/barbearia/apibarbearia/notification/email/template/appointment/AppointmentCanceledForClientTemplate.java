package br.com.barbearia.apibarbearia.notification.email.template.appointment;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Template de CANCELAMENTO DE AGENDAMENTO — enviado ao cliente.
 *
 * ══ TEMA ══
 * LIGHT MODE FORÇADO — mesmas técnicas do template de confirmação:
 *   • color-scheme: light only
 *   • bgcolor em todos os containers
 *   • background-color inline
 *   • Fonte Arial/Helvetica
 *   • Comentários condicionais para Outlook
 *
 * ══ ASSINATURA ══
 * Ambas as sobrecargas html() mantidas — zero breaking change.
 */
@Component
public class AppointmentCanceledForClientTemplate {

    private final EmailLayout layout;

    public AppointmentCanceledForClientTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Agendamento cancelado — " + layout.appName();
    }

    /** Sobrecarga retrocompatível (sem detalhes de cancelamento) */
    public String html(String clientName, String professionalName,
                       String serviceName, LocalDateTime startAt) {
        return html(clientName, professionalName, serviceName, startAt, null, null, null);
    }

    /** Assinatura IDÊNTICA ao original */
    public String html(String clientName, String professionalName, String serviceName,
                       LocalDateTime startAt, String canceledBy,
                       String cancelOrigin, String cancelMessage) {

        String day      = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String time     = startAt.format(DateTimeFormatter.ofPattern("HH:mm"));
        boolean byClient = "CLIENT".equals(cancelOrigin);

        return wrapper(
                hero(layout.escape(clientName), byClient) +
                        detailCard(layout.escape(serviceName), layout.escape(professionalName),
                                day, time, canceledBy, byClient) +
                        (cancelMessage != null && !cancelMessage.isBlank()
                                ? messageBox(layout.escape(cancelMessage)) : "") +
                        rebookSection()
        );
    }

    // ── Seções ────────────────────────────────────────────────────────────────

    private String hero(String clientName, boolean byClient) {
        String subtitle = byClient
                ? "Seu agendamento foi cancelado conforme solicitado."
                : "Seu agendamento foi cancelado pela barbearia.";
        return
                "<div bgcolor='#ef4444' style='background-color:#ef4444;"
                        + "padding:32px 32px 24px;text-align:center;"
                        + "border-radius:12px 12px 0 0;'>"
                        + "  <div style='display:inline-block;background:rgba(255,255,255,0.2);"
                        + "       border:2px solid rgba(255,255,255,0.5);border-radius:50%;"
                        + "       width:52px;height:52px;line-height:52px;font-size:20px;"
                        + "       margin-bottom:12px;color:#ffffff;'>✕</div>"
                        + "  <h1 style='margin:0 0 6px;font-size:21px;font-weight:800;color:#ffffff;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>Cancelamento Confirmado</h1>"
                        + "  <p style='margin:0;font-size:13px;color:rgba(255,255,255,0.9);"
                        + "       font-family:Arial,Helvetica,sans-serif;'>"
                        + "       Olá, <strong>" + clientName + "</strong> — " + subtitle + "</p>"
                        + "</div>";
    }

    private String detailCard(String service, String professional,
                              String day, String time,
                              String canceledBy, boolean byClient) {
        StringBuilder rows = new StringBuilder();
        rows.append(row("🪒", "Serviço",      service));
        rows.append(row("👤", "Profissional", professional));
        rows.append(row("📅", "Data",         day + " às " + time));
        if (canceledBy != null && !canceledBy.isBlank() && !byClient) {
            rows.append(row("🧑‍💼", "Cancelado por", layout.escape(canceledBy)));
        }
        return
                "<div bgcolor='#ffffff' style='background-color:#ffffff;padding:24px 32px;"
                        + "border-left:4px solid #ef4444;border-bottom:1px solid #f0f0f0;'>"
                        + "  <p style='margin:0 0 14px;font-size:10px;font-weight:700;letter-spacing:1.5px;"
                        + "       color:#ef4444;text-transform:uppercase;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>Dados do Agendamento</p>"
                        + rows
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

    private String messageBox(String message) {
        return
                "<div bgcolor='#ffffff' style='background-color:#ffffff;padding:0 32px 18px;'>"
                        + "  <div bgcolor='#fffbeb' style='background-color:#fffbeb;"
                        + "       border:1px solid #fde68a;border-left:4px solid #f59e0b;"
                        + "       border-radius:0 8px 8px 0;padding:14px;'>"
                        + "    <p style='margin:0 0 4px;font-size:10px;font-weight:700;"
                        + "         letter-spacing:1px;color:#92400e;text-transform:uppercase;"
                        + "         font-family:Arial,Helvetica,sans-serif;'>💬 Motivo Informado</p>"
                        + "    <p style='margin:0;font-size:13px;color:#78350f;font-style:italic;"
                        + "         line-height:1.5;font-family:Arial,Helvetica,sans-serif;'>"
                        + "         \"" + message + "\"</p>"
                        + "  </div>"
                        + "</div>";
    }

    private String rebookSection() {
        String url = layout.frontendUrl() + "/agendar";
        return
                "<div bgcolor='#065f46' style='background-color:#065f46;"
                        + "background:linear-gradient(135deg,#065f46 0%,#10b981 100%);"
                        + "padding:22px 32px;text-align:center;'>"
                        + "  <p style='margin:0 0 4px;font-size:17px;font-weight:800;color:#ffffff;"
                        + "       font-family:Arial,Helvetica,sans-serif;'>Que tal reagendar? 💈</p>"
                        + "  <p style='margin:0 0 14px;font-size:12px;color:rgba(255,255,255,0.85);"
                        + "       font-family:Arial,Helvetica,sans-serif;'>"
                        + "       Ainda temos horários disponíveis para você.</p>"
                        + "  <a href='" + url + "' "
                        + "     style='display:inline-block;padding:10px 28px;background-color:#ffffff;"
                        + "     color:#065f46;border-radius:50px;font-size:13px;font-weight:700;"
                        + "     text-decoration:none;font-family:Arial,Helvetica,sans-serif;'>"
                        + "     Fazer Novo Agendamento</a>"
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
                        + "  <meta name='color-scheme' content='light'>"
                        + "  <meta name='supported-color-schemes' content='light'>"
                        + "  <title>Agendamento Cancelado</title>"
                        + "  <style>"
                        + "    :root { color-scheme: light only; }"
                        + "    body  { background-color: #f3f4f6 !important; }"
                        + "    [data-ogsc] body { background-color: #f3f4f6 !important; }"
                        + "  </style>"
                        + "</head>"
                        + "<body bgcolor='#f3f4f6' "
                        + "style='margin:0;padding:0;background-color:#f3f4f6;"
                        + "font-family:Arial,Helvetica,sans-serif;-webkit-text-size-adjust:100%;'>"
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