package br.com.barbearia.apibarbearia.appointment.events;

import br.com.barbearia.apibarbearia.notification.email.appointment.AppointmentEmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Escuta eventos de agendamento e delega os disparos de e-mail.
 *
 * Única mudança necessária em relação ao seu listener original:
 *   - sendAppointmentCreatedToClient agora recebe appointmentId
 *   - sendCanceledToClient agora recebe appointmentId e code
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventListener {

    private final AppointmentEmailNotificationService emailService;

    @Async
    @EventListener
    public void onAppointmentChanged(AppointmentChangedEvent event) {
        try {
            switch (event.getType()) {
                case CREATED:   handleCreated(event);   break;
                case CANCELED:  handleCanceled(event);  break;
                case CONFIRMED: handleConfirmed(event); break;
                default:
                    log.debug("[EVENT] Tipo não tratado: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("[EVENT] Erro ao processar {}: {}", event.getType(), e.getMessage(), e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    private void handleCreated(AppointmentChangedEvent e) {
        // Cliente — com PDF anexado
        if (hasEmail(e.getClientEmail())) {
            emailService.sendAppointmentCreatedToClient(
                    e.getClientEmail(),
                    e.getClientName(),
                    e.getProfessionalName(),
                    e.getServiceName(),
                    e.getStartAt(),
                    e.getDurationMinutes(),
                    e.getAppointmentCode(),
                    e.getCancelLink(),
                    e.getAppointmentId()   // ← novo: para gerar o PDF
            );
        }

        // Profissional — sem PDF
        if (hasEmail(e.getProfessionalEmail())) {
            emailService.sendNewAppointmentToProfessional(
                    e.getProfessionalEmail(),
                    e.getProfessionalName(),
                    e.getClientName(),
                    e.getServiceName(),
                    e.getStartAt()
            );
        }
    }

    private void handleCanceled(AppointmentChangedEvent e) {
        // Cliente — com PDF cancelado anexado
        if (hasEmail(e.getClientEmail())) {
            emailService.sendCanceledToClient(
                    e.getClientEmail(),
                    e.getClientName(),
                    e.getProfessionalName(),
                    e.getServiceName(),
                    e.getStartAt(),
                    e.getCanceledByUsername(),
                    e.getCancelOrigin(),
                    e.getCancelMessage(),
                    e.getAppointmentId(),  // ← novo
                    e.getAppointmentCode() // ← novo (para nomear o arquivo)
            );
        }

        // Profissional — sem PDF
        if (hasEmail(e.getProfessionalEmail())) {
            emailService.sendCanceledToProfessional(
                    e.getProfessionalEmail(),
                    e.getProfessionalName(),
                    e.getClientName(),
                    e.getServiceName(),
                    e.getStartAt(),
                    e.getCanceledByUsername(),
                    e.getCancelOrigin(),
                    e.getCancelMessage()
            );
        }
    }

    private void handleConfirmed(AppointmentChangedEvent e) {
        // Confirmação não dispara e-mail ao cliente por padrão
        log.debug("[EVENT] Chegada confirmada: {}", e.getAppointmentCode());
    }

    private boolean hasEmail(String email) {
        return email != null && !email.isBlank();
    }
}