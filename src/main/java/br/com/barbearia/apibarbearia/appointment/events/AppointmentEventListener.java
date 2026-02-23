package br.com.barbearia.apibarbearia.appointment.events;

import br.com.barbearia.apibarbearia.notification.email.appointment.AppointmentEmailNotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventListener {

    private final AppointmentEmailNotificationService emailService;

    public AppointmentEventListener(AppointmentEmailNotificationService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    public void handle(AppointmentChangedEvent e) {

        if (e.getType() == AppointmentEventType.CREATED) {
            emailService.sendNewAppointmentToProfessional(
                    e.getProfessionalEmail(),
                    e.getProfessionalName(),
                    e.getClientName(),
                    e.getServiceName(),
                    e.getStartAt()
            );

            emailService.sendAppointmentCreatedToClient(
                    e.getClientEmail(),
                    e.getClientName(),
                    e.getProfessionalName(),
                    e.getServiceName(),
                    e.getStartAt(),
                    e.getCancelLink()
            );
            return;
        }

        if (e.getType() == AppointmentEventType.CANCELED) {
            emailService.sendCanceledToClient(
                    e.getClientEmail(),
                    e.getClientName(),
                    e.getProfessionalName(),
                    e.getServiceName(),
                    e.getStartAt()
            );

            emailService.sendCanceledToProfessional(
                    e.getProfessionalEmail(),
                    e.getProfessionalName(),
                    e.getClientName(),
                    e.getServiceName(),
                    e.getStartAt()
            );
            return;
        }

        if (e.getType() == AppointmentEventType.REMINDER_30MIN) {
            emailService.sendReminderToClient(
                    e.getClientEmail(),
                    e.getClientName(),
                    e.getProfessionalName(),
                    e.getServiceName(),
                    e.getStartAt()
            );

            emailService.sendReminderToProfessional(
                    e.getProfessionalEmail(),
                    e.getProfessionalName(),
                    e.getClientName(),
                    e.getServiceName(),
                    e.getStartAt()
            );
        }
    }
}
