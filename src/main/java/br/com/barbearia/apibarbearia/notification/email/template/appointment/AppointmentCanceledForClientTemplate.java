package br.com.barbearia.apibarbearia.notification.email.template.appointment;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentCanceledForClientTemplate {

    private final EmailLayout layout;

    public AppointmentCanceledForClientTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Agendamento cancelado - " + layout.appName();
    }

    public String html(String clientName, String professionalName, String serviceName, LocalDateTime startAt) {
        String when = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String content =
                layout.paragraph("Olá, " + clientName + ".") +
                        layout.paragraph("Seu agendamento foi cancelado com sucesso ✅") +
                        layout.infoRow("SERVIÇO", serviceName) +
                        layout.infoRow("PROFISSIONAL", professionalName) +
                        layout.infoRow("DATA/HORA", when);

        return layout.baseTemplate("Cancelamento Confirmado", "Status atualizado", content, null, null);
    }
}
