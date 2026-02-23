package br.com.barbearia.apibarbearia.notification.email.appointment;


import br.com.barbearia.apibarbearia.notification.email.sender.EmailSender;
import br.com.barbearia.apibarbearia.notification.email.template.appointment.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AppointmentEmailNotificationService {

    private final EmailSender emailSender;

    private final NewAppointmentForProfessionalTemplate newForProfessional;
    private final AppointmentCreatedForClientTemplate createdForClient;
    private final AppointmentCanceledForClientTemplate canceledForClient;
    private final AppointmentCanceledForProfessionalTemplate canceledForProfessional;
    private final AppointmentReminderForClientTemplate reminderForClient;
    private final AppointmentReminderForProfessionalTemplate reminderForProfessional;

    public AppointmentEmailNotificationService(
            EmailSender emailSender,
            NewAppointmentForProfessionalTemplate newForProfessional,
            AppointmentCreatedForClientTemplate createdForClient,
            AppointmentCanceledForClientTemplate canceledForClient,
            AppointmentCanceledForProfessionalTemplate canceledForProfessional,
            AppointmentReminderForClientTemplate reminderForClient,
            AppointmentReminderForProfessionalTemplate reminderForProfessional
    ) {
        this.emailSender = emailSender;
        this.newForProfessional = newForProfessional;
        this.createdForClient = createdForClient;
        this.canceledForClient = canceledForClient;
        this.canceledForProfessional = canceledForProfessional;
        this.reminderForClient = reminderForClient;
        this.reminderForProfessional = reminderForProfessional;
    }

    @Async
    public void sendNewAppointmentToProfessional(String to, String professionalName, String clientName, String serviceName, LocalDateTime startAt) {
        emailSender.sendHtml(to, newForProfessional.subject(), newForProfessional.html(professionalName, clientName, serviceName, startAt));
    }

    @Async
    public void sendAppointmentCreatedToClient(String to, String clientName, String professionalName, String serviceName, LocalDateTime startAt, String cancelLink) {
        emailSender.sendHtml(to, createdForClient.subject(), createdForClient.html(clientName, professionalName, serviceName, startAt, cancelLink));
    }

    @Async
    public void sendCanceledToClient(String to, String clientName, String professionalName, String serviceName, LocalDateTime startAt) {
        emailSender.sendHtml(to, canceledForClient.subject(), canceledForClient.html(clientName, professionalName, serviceName, startAt));
    }

    @Async
    public void sendCanceledToProfessional(String to, String professionalName, String clientName, String serviceName, LocalDateTime startAt) {
        emailSender.sendHtml(to, canceledForProfessional.subject(), canceledForProfessional.html(professionalName, clientName, serviceName, startAt));
    }

    @Async
    public void sendReminderToClient(String to, String clientName, String professionalName, String serviceName, LocalDateTime startAt) {
        emailSender.sendHtml(to, reminderForClient.subject(), reminderForClient.html(clientName, professionalName, serviceName, startAt));
    }

    @Async
    public void sendReminderToProfessional(String to, String professionalName, String clientName, String serviceName, LocalDateTime startAt) {
        emailSender.sendHtml(to, reminderForProfessional.subject(), reminderForProfessional.html(professionalName, clientName, serviceName, startAt));
    }
}
