package br.com.barbearia.apibarbearia.notification.email.schedule;


import br.com.barbearia.apibarbearia.notification.email.sender.EmailSender;
import br.com.barbearia.apibarbearia.notification.email.template.schedule.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ScheduleEmailNotificationService {

    private final EmailSender emailSender;

    private final WorkingHoursUpdatedTemplate workingHoursUpdatedTemplate;
    private final BreakCreatedTemplate breakCreatedTemplate;
    private final BreakDeletedTemplate breakDeletedTemplate;
    private final DayOverrideUpdatedTemplate dayOverrideUpdatedTemplate;

    public ScheduleEmailNotificationService(
            EmailSender emailSender,
            WorkingHoursUpdatedTemplate workingHoursUpdatedTemplate,
            BreakCreatedTemplate breakCreatedTemplate,
            BreakDeletedTemplate breakDeletedTemplate,
            DayOverrideUpdatedTemplate dayOverrideUpdatedTemplate
    ) {
        this.emailSender = emailSender;
        this.workingHoursUpdatedTemplate = workingHoursUpdatedTemplate;
        this.breakCreatedTemplate = breakCreatedTemplate;
        this.breakDeletedTemplate = breakDeletedTemplate;
        this.dayOverrideUpdatedTemplate = dayOverrideUpdatedTemplate;
    }

    @Async
    public void sendWorkingHoursUpdated(String to, String nome, String profissionalNome, String dayOfWeek,
                                        String startTime, String endTime, String updatedBy) {
        emailSender.sendHtml(to,
                workingHoursUpdatedTemplate.subject(),
                workingHoursUpdatedTemplate.html(nome, profissionalNome, dayOfWeek, startTime, endTime, updatedBy));
    }

    @Async
    public void sendBreakCreated(String to, String nome, String profissionalNome, String dayOfWeek,
                                 String startTime, String endTime, String createdBy) {
        emailSender.sendHtml(to,
                breakCreatedTemplate.subject(),
                breakCreatedTemplate.html(nome, profissionalNome, dayOfWeek, startTime, endTime, createdBy));
    }

    @Async
    public void sendBreakDeleted(String to, String nome, String profissionalNome, String dayOfWeek,
                                 String startTime, String endTime, String deletedBy) {
        emailSender.sendHtml(to,
                breakDeletedTemplate.subject(),
                breakDeletedTemplate.html(nome, profissionalNome, dayOfWeek, startTime, endTime, deletedBy));
    }

    @Async
    public void sendDayOverrideUpdated(String to, String nome, String profissionalNome, String date,
                                       String status, String timeRange, String updatedBy) {
        emailSender.sendHtml(to,
                dayOverrideUpdatedTemplate.subject(),
                dayOverrideUpdatedTemplate.html(nome, profissionalNome, date, status, timeRange, updatedBy));
    }
}
