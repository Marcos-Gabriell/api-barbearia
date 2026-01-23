package br.com.barbearia.apibarbearia.notification.email.template.schedule;


import br.com.barbearia.apibarbearia.notification.email.template.EmailLayout;
import org.springframework.stereotype.Service;

@Service
public class BreakDeletedTemplate {

    private final EmailLayout layout;

    public BreakDeletedTemplate(EmailLayout layout) {
        this.layout = layout;
    }

    public String subject() {
        return "Agenda atualizada (pausa removida)";
    }

    public String html(String nome, String profissionalNome, String dayOfWeek, String startTime, String endTime, String deletedBy) {
        String title = "Pausa removida ✅";
        String subtitle = "Olá " + layout.escape(nome) + ", uma pausa foi removida da agenda.";

        String content =
                layout.infoRow("PROFISSIONAL", layout.escape(profissionalNome)) +
                        layout.infoRow("DIA", layout.escape(dayOfWeek)) +
                        layout.infoRow("PAUSA", layout.escape(startTime + " - " + endTime)) +
                        "<div style='margin-top:16px;'></div>" +
                        layout.note("Removida por: " + layout.escape(deletedBy));

        String ctaUrl = layout.frontendUrl() + "/agenda";
        return layout.baseTemplate(title, subtitle, content, "Ver Agenda", ctaUrl);
    }
}
