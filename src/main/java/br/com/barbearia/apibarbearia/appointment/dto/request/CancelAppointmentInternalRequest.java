package br.com.barbearia.apibarbearia.appointment.dto.request;

import lombok.*;

import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentInternalRequest {

    @Size(max = 300, message = "Mensagem pode ter no máximo 300 caracteres.")
    private String message;

}
