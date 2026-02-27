package br.com.barbearia.apibarbearia.notification.email.template.appointment;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentCanceledForProfessionalTemplate {

    private final EmailLayout layout;

    public AppointmentCanceledForProfessionalTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Agendamento cancelado - " + layout.appName();
    }
    
    public String html(String professionalName, String clientName, String serviceName,
                       LocalDateTime startAt, String canceledBy, String cancelMessage) {
        return html(professionalName, clientName, serviceName, startAt, canceledBy, null, cancelMessage);
    }

    public String html(String professionalName, String clientName, String serviceName,
                       LocalDateTime startAt, String canceledBy, String cancelOrigin, String cancelMessage) {

        String when = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder content = new StringBuilder();
        content.append(layout.paragraph("Olá, " + professionalName + "."));

        if ("CLIENT".equals(cancelOrigin)) {
            content.append(layout.paragraph("O cliente cancelou o agendamento."));
        } else {
            content.append(layout.paragraph("Um agendamento foi cancelado pelo sistema/administração."));
        }

        content.append(layout.infoRow("CLIENTE", clientName));
        content.append(layout.infoRow("SERVIÇO", serviceName));
        content.append(layout.infoRow("DATA/HORA", when));
        content.append(layout.infoRow("CANCELADO POR", canceledBy != null ? canceledBy : "Sistema"));

        if (cancelMessage != null && !cancelMessage.isBlank()) {
            content.append("<div style='background:#fef3c7;padding:15px;border-radius:8px;border-left:4px solid #f59e0b;margin:15px 0;'>");
            content.append("<p style='margin:0 0 5px;font-size:11px;font-weight:700;color:#92400e;'>💬 MOTIVO INFORMADO:</p>");
            content.append("<p style='margin:0;font-size:13px;color:#78350f;font-style:italic;'>\"" + layout.escape(cancelMessage) + "\"</p>");
            content.append("</div>");
        }

        return layout.baseTemplate("Agendamento Cancelado", "Agenda liberada", content.toString(), null, null);
    }
}