package br.com.barbearia.apibarbearia.appointment.dto.request;
import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentInternalRequest {

    @NotBlank(message = "Nome do cliente é obrigatório.")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String clientName;

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres.")
    private String clientEmail;

    @NotBlank(message = "Telefone é obrigatório.")
    @Size(min = 11, max = 15, message = "Telefone deve ter entre 11 e 15 dígitos.")
    private String clientPhone;

    @NotNull(message = "serviceId é obrigatório.")
    private Long serviceId;

    @NotNull(message = "professionalUserId é obrigatório.")
    private Long professionalUserId;

    @NotNull(message = "startAt é obrigatório.")
    private LocalDateTime startAt;
}
