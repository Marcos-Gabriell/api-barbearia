package br.com.barbearia.apibarbearia.notification.email.template.schedule;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class WorkingHoursUpdatedTemplate {

    private final EmailLayout layout;

    public WorkingHoursUpdatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Agenda atualizada (horário de funcionamento)";
    }

    public String html(String nome, String profissionalNome, String dayOfWeek, String startTime, String endTime, String updatedBy) {
        String title = "Horário atualizado ✅";
        String subtitle = "Olá " + layout.escape(nome) + ", o horário de funcionamento foi atualizado.";

        String content =
                layout.infoRow("PROFISSIONAL", layout.escape(profissionalNome)) +
                        layout.infoRow("DIA", layout.escape(dayOfWeek)) +
                        layout.infoRow("HORÁRIO", layout.escape(startTime + " - " + endTime)) +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Atualizado por: " + layout.escape(updatedBy));

        String ctaUrl = layout.frontendUrl() + "/agenda";
        return layout.baseTemplate(title, subtitle, content, "Ver Agenda", ctaUrl);
    }
}
