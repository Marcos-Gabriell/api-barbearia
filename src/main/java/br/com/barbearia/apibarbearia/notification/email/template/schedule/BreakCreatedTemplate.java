package br.com.barbearia.apibarbearia.notification.email.template.schedule;


import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class BreakCreatedTemplate {

    private final EmailLayout layout;

    public BreakCreatedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Agenda atualizada (pausa adicionada)";
    }

    public String html(String nome, String profissionalNome, String dayOfWeek, String startTime, String endTime, String createdBy) {
        String title = "Pausa adicionada ☕";
        String subtitle = "Olá " + layout.escape(nome) + ", uma pausa foi adicionada na agenda.";

        String content =
                layout.infoRow("PROFISSIONAL", layout.escape(profissionalNome)) +
                        layout.infoRow("DIA", layout.escape(dayOfWeek)) +
                        layout.infoRow("PAUSA", layout.escape(startTime + " - " + endTime)) +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Adicionada por: " + layout.escape(createdBy));

        String ctaUrl = layout.frontendUrl() + "/agenda";
        return layout.baseTemplate(title, subtitle, content, "Ver Agenda", ctaUrl);
    }
}
