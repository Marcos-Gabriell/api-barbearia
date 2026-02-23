package br.com.barbearia.apibarbearia.notification.email.template.appointment;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentCreatedForClientTemplate {

    private final EmailLayout layout;

    public AppointmentCreatedForClientTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Seu agendamento foi registrado - " + layout.appName();
    }

    public String html(String clientName, String professionalName, String serviceName, LocalDateTime startAt, String cancelLink) {
        String when = startAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String content =
                layout.paragraph("Olá, " + clientName + ".") +
                        layout.paragraph("Seu agendamento foi registrado com sucesso ✅") +
                        layout.infoRow("SERVIÇO", serviceName) +
                        layout.infoRow("PROFISSIONAL", professionalName) +
                        layout.infoRow("DATA/HORA", when) +
                        layout.warning("Você pode cancelar até 10 minutos antes do horário.") +
                        layout.button("Cancelar agendamento", cancelLink);

        return layout.baseTemplate("Agendamento Confirmado", "Veja os detalhes", content, null, null);
    }
}
