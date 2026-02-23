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

    public String html(String professionalName, String clientName, String serviceName, LocalDateTime startAt) {
        String when = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String content =
                layout.paragraph("Olá, " + professionalName + ".") +
                        layout.paragraph("Um agendamento foi cancelado ✅") +
                        layout.infoRow("CLIENTE", clientName) +
                        layout.infoRow("SERVIÇO", serviceName) +
                        layout.infoRow("DATA/HORA", when);

        return layout.baseTemplate("Agendamento Cancelado", "Agenda liberada", content, null, null);
    }
}
