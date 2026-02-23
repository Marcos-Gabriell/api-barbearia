package br.com.barbearia.apibarbearia.appointment.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@RequiredArgsConstructor
public class AppointmentChangedEvent {

    private final AppointmentEventType type;
    private final Long appointmentId;
    private final String appointmentCode; // <-- ADICIONE ESTA LINHA

    private final String clientName;
    private final String clientEmail;

    private final String professionalName;
    private final String professionalEmail;

    private final String serviceName;
    private final LocalDateTime startAt;

    private final String cancelLink;
}