package br.com.barbearia.apibarbearia.appointment.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreatedResponse {

    private String message;

    private Long appointmentId;

    private String status;

    private String serviceName;

    private String professionalName;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}