package br.com.barbearia.apibarbearia.notification.email.template.schedule;

import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class DayOverrideUpdatedTemplate {

    private final EmailLayout layout;

    public DayOverrideUpdatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Agenda atualizada (exceção de funcionamento)";
    }

    public String html(String nome, String profissionalNome, String date, String status, String timeRange, String updatedBy) {
        String title = "Funcionamento do dia atualizado 📅";
        String subtitle = "Olá " + layout.escape(nome) + ", o funcionamento de um dia específico foi ajustado.";

        String content =
                layout.infoRow("PROFISSIONAL", layout.escape(profissionalNome)) +
                        layout.infoRow("DATA", layout.escape(date)) +
                        layout.infoRow("STATUS", layout.escape(status)) +
                        layout.infoRow("HORÁRIO", layout.escape(timeRange)) +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Atualizado por: " + layout.escape(updatedBy));

        String ctaUrl = layout.frontendUrl() + "/agenda";
        return layout.baseTemplate(title, subtitle, content, "Ver Agenda", ctaUrl);
    }
}
