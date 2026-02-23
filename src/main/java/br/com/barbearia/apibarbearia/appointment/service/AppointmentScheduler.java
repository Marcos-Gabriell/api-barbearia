package br.com.barbearia.apibarbearia.appointment.service;

import br.com.barbearia.apibarbearia.appointment.entity.Appointment;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentCancelReason;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import br.com.barbearia.apibarbearia.appointment.events.*;
import br.com.barbearia.apibarbearia.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler para tarefas automáticas de agendamento:
 *
 * 1. Lembrete 30 minutos antes (para agendamentos CONFIRMED)
 * 2. Marcação automática de NO_SHOW após 10 minutos do horário
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher publisher;

    /**
     * Envia lembrete 30 minutos antes do agendamento.
     * Executa a cada 1 minuto.
     *
     * Só envia para agendamentos com status CONFIRMED ou PENDING.
     */
    @Scheduled(fixedDelay = 60000) // 1 minuto
    @Transactional(readOnly = true)
    public void sendReminder30Min() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusMinutes(29);
        LocalDateTime to = now.plusMinutes(31);

        // Lembrete para PENDING (ainda não chegou) e CONFIRMED (caso tenha sido confirmado antecipadamente)
        List<Appointment> pendingList = appointmentRepository.findAllByStatusAndStartAtBetween(
                AppointmentStatus.PENDING, from, to);

        for (Appointment a : pendingList) {
            log.info("Enviando lembrete 30min para agendamento {} (código: {})", a.getId(), a.getCode());

            publisher.publishEvent(AppointmentChangedEvent.builder()
                    .type(AppointmentEventType.REMINDER_30MIN)
                    .appointmentId(a.getId())
                    .appointmentCode(a.getCode())
                    .clientName(a.getClientName())
                    .clientEmail(a.getClientEmail())
                    .professionalName(a.getProfessionalName())
                    .professionalEmail(a.getProfessionalEmail())
                    .serviceName(a.getServiceName())
                    .startAt(a.getStartAt())
                    .build());
        }
    }

    /**
     * Marca automaticamente como NO_SHOW agendamentos PENDING que passaram 10 minutos do horário.
     * Executa a cada 1 minuto.
     *
     * Regra: Se o agendamento está PENDING e já passou 10 minutos do startAt, marca como NO_SHOW.
     */
    @Scheduled(fixedDelay = 60000) // 1 minuto
    @Transactional
    public void autoNoShowAfter10Min() {
        // Agendamentos PENDING que já passaram 10 minutos do horário
        LocalDateTime limit = LocalDateTime.now().minusMinutes(10);

        List<Appointment> list = appointmentRepository.findAllByStatusAndStartAtBefore(
                AppointmentStatus.PENDING, limit);

        LocalDateTime now = LocalDateTime.now();

        for (Appointment a : list) {
            // Dupla verificação de segurança
            if (a.getStatus() == AppointmentStatus.CANCELLED ||
                    a.getStatus() == AppointmentStatus.NO_SHOW ||
                    a.getStatus() == AppointmentStatus.CONFIRMED) {
                continue;
            }

            log.info("Marcando agendamento {} (código: {}) como NO_SHOW automaticamente - passou 10min do horário",
                    a.getId(), a.getCode());

            a.setStatus(AppointmentStatus.NO_SHOW);
            a.setCanceledAt(now);
            a.setCancelReason(AppointmentCancelReason.SYSTEM_NO_SHOW);
            a.setUpdatedAt(now);

            appointmentRepository.save(a);

            // Notifica sobre o no-show
            publisher.publishEvent(AppointmentChangedEvent.builder()
                    .type(AppointmentEventType.NO_SHOW)
                    .appointmentId(a.getId())
                    .appointmentCode(a.getCode())
                    .clientName(a.getClientName())
                    .clientEmail(a.getClientEmail())
                    .professionalName(a.getProfessionalName())
                    .professionalEmail(a.getProfessionalEmail())
                    .serviceName(a.getServiceName())
                    .startAt(a.getStartAt())
                    .build());
        }
    }
}