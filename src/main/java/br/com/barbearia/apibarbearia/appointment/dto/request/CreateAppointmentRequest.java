package br.com.barbearia.apibarbearia.appointment.dto.request;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotBlank(message = "Nome do cliente é obrigatório.")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String clientName;

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 150)
    private String clientEmail;

    @NotBlank(message = "Telefone é obrigatório.")
    @Size(min = 11, max = 15, message = "Telefone inválido.")
    private String clientPhone;

    @NotNull(message = "serviceId é obrigatório.")
    private Long serviceId;

    @NotNull(message = "professionalUserId é obrigatório.")
    private Long professionalUserId;

    @NotNull(message = "startAt é obrigatório.")
    private LocalDateTime startAt;

}