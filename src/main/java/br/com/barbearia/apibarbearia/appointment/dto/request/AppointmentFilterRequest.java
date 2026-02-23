package br.com.barbearia.apibarbearia.appointment.dto.request;

import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO para filtros de listagem de agendamentos.
 */
@Getter
@Setter
public class AppointmentFilterRequest {

    private String q;

    private String clientName;

    private String clientEmail;

    private String clientPhone;

    private AppointmentStatus status;

    private Long professionalUserId;

    private Long serviceId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private Integer page;

    private Integer size;

    private String sort;
}