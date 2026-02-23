package br.com.barbearia.apibarbearia.notification.email.template.appointment;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NewAppointmentForProfessionalTemplate {

    private final EmailLayout layout;

    public NewAppointmentForProfessionalTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Novo agendamento - " + layout.appName();
    }

    public String html(String professionalName, String clientName, String serviceName, LocalDateTime startAt) {
        String when = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String content =
                layout.paragraph("Olá, " + professionalName + ".") +
                        layout.paragraph("Você recebeu um novo agendamento ✅") +
                        layout.infoRow("CLIENTE", clientName) +
                        layout.infoRow("SERVIÇO", serviceName) +
                        layout.infoRow("DATA/HORA", when) +
                        layout.warning("Caso necessário, você pode cancelar até 10 minutos antes do horário.");

        return layout.baseTemplate("Novo Agendamento", "Confira os detalhes", content, null, null);
    }
}
