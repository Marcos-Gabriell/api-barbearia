package br.com.barbearia.apibarbearia.appointment.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlotResponse {

    private LocalDate date;

    private LocalTime start;

    private LocalTime end;

}
