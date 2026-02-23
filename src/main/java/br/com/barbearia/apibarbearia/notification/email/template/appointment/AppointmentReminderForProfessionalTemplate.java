package br.com.barbearia.apibarbearia.notification.email.template.appointment;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentReminderForProfessionalTemplate {

    private final EmailLayout layout;

    public AppointmentReminderForProfessionalTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Lembrete de atendimento - " + layout.appName();
    }

    public String html(String professionalName, String clientName, String serviceName, LocalDateTime startAt) {
        String when = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String content =
                layout.paragraph("Olá, " + professionalName + ".") +
                        layout.paragraph("Você tem um atendimento em breve ⏰") +
                        layout.infoRow("CLIENTE", clientName) +
                        layout.infoRow("SERVIÇO", serviceName) +
                        layout.infoRow("DATA/HORA", when);

        return layout.baseTemplate("Lembrete", "Atendimento próximo", content, null, null);
    }
}
