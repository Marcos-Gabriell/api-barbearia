package br.com.barbearia.apibarbearia.notification.email.appointment;

import br.com.barbearia.apibarbearia.appointment.service.AppointmentPdfService;
import br.com.barbearia.apibarbearia.notification.email.sender.EmailSender;
import br.com.barbearia.apibarbearia.notification.email.template.appointment.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;

/**
 * Serviço de notificações por e-mail para agendamentos.
 *
 * Extensão do serviço original: mantém todos os métodos existentes
 * e adiciona envio de PDF como anexo nos e-mails de criação e cancelamento.
 *
 * Compatível 100% com o AppointmentEventListener existente — basta
 * adicionar o parâmetro appointmentId nos dois métodos modificados.
 */
@Slf4j
@Service
public class AppointmentEmailNotificationService {

    private final EmailSender                                emailSender;
    private final JavaMailSender                             mailSender;
    private final AppointmentPdfService                      pdfService;
    private final NewAppointmentForProfessionalTemplate      newForProfessional;
    private final AppointmentCreatedForClientTemplate        createdForClient;
    private final AppointmentCanceledForClientTemplate       canceledForClient;
    private final AppointmentCanceledForProfessionalTemplate canceledForProfessional;
    private final AppointmentReminderForClientTemplate       reminderForClient;
    private final AppointmentReminderForProfessionalTemplate reminderForProfessional;

    public AppointmentEmailNotificationService(
            EmailSender emailSender,
            JavaMailSender mailSender,
            AppointmentPdfService pdfService,
            NewAppointmentForProfessionalTemplate newForProfessional,
            AppointmentCreatedForClientTemplate createdForClient,
            AppointmentCanceledForClientTemplate canceledForClient,
            AppointmentCanceledForProfessionalTemplate canceledForProfessional,
            AppointmentReminderForClientTemplate reminderForClient,
            AppointmentReminderForProfessionalTemplate reminderForProfessional
    ) {
        this.emailSender           = emailSender;
        this.mailSender            = mailSender;
        this.pdfService            = pdfService;
        this.newForProfessional    = newForProfessional;
        this.createdForClient      = createdForClient;
        this.canceledForClient     = canceledForClient;
        this.canceledForProfessional = canceledForProfessional;
        this.reminderForClient     = reminderForClient;
        this.reminderForProfessional = reminderForProfessional;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NOVO AGENDAMENTO — PROFISSIONAL (sem anexo)
    // ════════════════════════════════════════════════════════════════════════

    @Async
    public void sendNewAppointmentToProfessional(
            String to, String professionalName, String clientName,
            String serviceName, LocalDateTime startAt
    ) {
        safeRun(() -> emailSender.sendHtml(
                to,
                newForProfessional.subject(),
                newForProfessional.html(professionalName, clientName, serviceName, startAt)
        ), "sendNewAppointmentToProfessional", to);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  AGENDAMENTO CRIADO — CLIENTE (com PDF anexado)
    //
    //  DIFERENÇA em relação ao original: adicionado parâmetro appointmentId.
    //  Atualizar a chamada no AppointmentEventListener para passar esse campo.
    // ════════════════════════════════════════════════════════════════════════

    @Async
    public void sendAppointmentCreatedToClient(
            String to,
            String clientName,
            String professionalName,
            String serviceName,
            LocalDateTime startAt,
            Integer durationMinutes,
            String code,
            String cancelLink,
            Long appointmentId          // NOVO — usado para gerar o PDF
    ) {
        safeRun(() -> {
            String html = createdForClient.html(clientName, professionalName, serviceName, startAt, cancelLink);
            byte[] pdf  = generatePdfSafely(appointmentId);

            if (pdf != null) {
                sendWithAttachment(to, createdForClient.subject(), html, pdf,
                        "comprovante-" + sanitize(code) + ".pdf");
            } else {
                // Fallback sem anexo se a geração do PDF falhar
                emailSender.sendHtml(to, createdForClient.subject(), html);
            }
        }, "sendAppointmentCreatedToClient", to);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CANCELAMENTO — CLIENTE (com PDF cancelado anexado)
    //
    //  DIFERENÇA: adicionados appointmentId e code para nomear o arquivo.
    // ════════════════════════════════════════════════════════════════════════

    @Async
    public void sendCanceledToClient(
            String to,
            String clientName,
            String professionalName,
            String serviceName,
            LocalDateTime startAt,
            String canceledByUsername,
            String cancelOrigin,
            String cancelMessage,
            Long appointmentId,         // NOVO
            String code                 // NOVO
    ) {
        safeRun(() -> {
            String html = canceledForClient.html(
                    clientName, professionalName, serviceName,
                    startAt, canceledByUsername, cancelOrigin, cancelMessage
            );
            byte[] pdf = generatePdfSafely(appointmentId);

            if (pdf != null) {
                sendWithAttachment(to, canceledForClient.subject(), html, pdf,
                        "cancelamento-" + sanitize(code) + ".pdf");
            } else {
                emailSender.sendHtml(to, canceledForClient.subject(), html);
            }
        }, "sendCanceledToClient", to);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CANCELAMENTO — PROFISSIONAL (sem PDF, assinatura original mantida)
    // ════════════════════════════════════════════════════════════════════════

    @Async
    public void sendCanceledToProfessional(
            String to, String professionalName, String clientName,
            String serviceName, LocalDateTime startAt,
            String canceledByUsername, String cancelOrigin, String cancelMessage
    ) {
        safeRun(() -> emailSender.sendHtml(
                to,
                canceledForProfessional.subject(),
                canceledForProfessional.html(
                        professionalName, clientName, serviceName,
                        startAt, canceledByUsername, cancelOrigin, cancelMessage
                )
        ), "sendCanceledToProfessional", to);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LEMBRETES (sem alteração)
    // ════════════════════════════════════════════════════════════════════════

    @Async
    public void sendReminderToClient(
            String to, String clientName, String professionalName,
            String serviceName, LocalDateTime startAt
    ) {
        safeRun(() -> emailSender.sendHtml(
                to, reminderForClient.subject(),
                reminderForClient.html(clientName, professionalName, serviceName, startAt)
        ), "sendReminderToClient", to);
    }

    @Async
    public void sendReminderToProfessional(
            String to, String professionalName, String clientName,
            String serviceName, LocalDateTime startAt
    ) {
        safeRun(() -> emailSender.sendHtml(
                to, reminderForProfessional.subject(),
                reminderForProfessional.html(professionalName, clientName, serviceName, startAt)
        ), "sendReminderToProfessional", to);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Envia e-mail HTML com PDF como anexo (multipart/mixed).
     * Usa MimeMessageHelper do Spring para garantir compatibilidade
     * com todos os clientes de e-mail.
     */
    private void sendWithAttachment(
            String to, String subject, String htmlBody,
            byte[] pdfBytes, String fileName
    ) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        // true = multipart | "UTF-8" = encoding
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = isHtml

        helper.addAttachment(fileName, new ByteArrayResource(pdfBytes), "application/pdf");

        mailSender.send(msg);
        log.info("[EMAIL] Enviado com PDF para {} | assunto: {}", to, subject);
    }

    /** Gera PDF sem lançar exceção — retorna null em caso de falha */
    private byte[] generatePdfSafely(Long appointmentId) {
        if (appointmentId == null) return null;
        try {
            return pdfService.generateReceipt(appointmentId);
        } catch (Exception e) {
            log.warn("[EMAIL] Falha ao gerar PDF para id={}: {}", appointmentId, e.getMessage());
            return null;
        }
    }

    /** Executa ação com tratamento de erro centralizado */
    private void safeRun(ThrowingRunnable action, String method, String to) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("[EMAIL] Falha em {} para {}: {}", method, to, e.getMessage(), e);
        }
    }

    private String sanitize(String code) {
        if (code == null) return "agendamento";
        return code.replaceAll("[^a-zA-Z0-9\\-_]", "").toLowerCase();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}