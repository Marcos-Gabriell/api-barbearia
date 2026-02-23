package br.com.barbearia.apibarbearia.appointment.dto.response;

import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;

    private String clientName;

    private String clientEmail;

    private String clientPhone;

    private Long serviceId;

    private String serviceName;

    private Integer durationMinutes;

    private Long professionalUserId;

    private String professionalName;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private AppointmentStatus status;

}
