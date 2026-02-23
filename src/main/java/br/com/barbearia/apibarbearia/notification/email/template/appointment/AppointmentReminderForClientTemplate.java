package br.com.barbearia.apibarbearia.notification.email.template.appointment;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentReminderForClientTemplate {

    private final EmailLayout layout;

    public AppointmentReminderForClientTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Lembrete do seu agendamento - " + layout.appName();
    }

    public String html(String clientName, String professionalName, String serviceName, LocalDateTime startAt) {
        String when = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String content =
                layout.paragraph("Olá, " + clientName + ".") +
                        layout.paragraph("Passando para lembrar do seu agendamento ⏰") +
                        layout.infoRow("SERVIÇO", serviceName) +
                        layout.infoRow("PROFISSIONAL", professionalName) +
                        layout.infoRow("DATA/HORA", when);

        return layout.baseTemplate("Lembrete", "Falta pouco!", content, null, null);
    }
}
