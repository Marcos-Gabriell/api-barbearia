package br.com.barbearia.apibarbearia.appointment.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Evento publicado após qualquer mudança relevante em um agendamento.
 * Consumido pelo AppointmentEventListener para disparar notificações.
 */
@Getter
@Builder
public class AppointmentChangedEvent {

    // ── Identificação ────────────────────────────────────────────────────
    private final AppointmentEventType type;
    private final Long                 appointmentId;
    private final String               appointmentCode;

    // ── Dados do cliente ────────────────────────────────────────────────
    private final String clientName;
    private final String clientEmail;

    // ── Dados do profissional ───────────────────────────────────────────
    private final String professionalName;
    private final String professionalEmail;

    // ── Dados do serviço ────────────────────────────────────────────────
    private final String       serviceName;
    private final Integer      durationMinutes;
    private final LocalDateTime startAt;

    // ── Criação ─────────────────────────────────────────────────────────
    private final LocalDateTime createdAt;
    private final String        createdByUsername;
    private final String        createdByRole;
    private final String        cancelLink;           // link de cancelamento via token

    // ── Confirmação ─────────────────────────────────────────────────────
    private final LocalDateTime confirmedAt;
    private final String        confirmedByUsername;
    private final String        confirmedByRole;

    // ── Cancelamento ────────────────────────────────────────────────────
    private final LocalDateTime canceledAt;
    private final String        canceledByUsername;
    private final String        cancelOrigin;         // "INTERNAL" | "CLIENT"
    private final String        cancelMessage;        // motivo (opcional)
}