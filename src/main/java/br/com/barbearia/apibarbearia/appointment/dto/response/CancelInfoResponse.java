package br.com.barbearia.apibarbearia.appointment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;


@Value
@Builder
public class CancelInfoResponse {

    /** Nome completo do cliente */
    String clientName;

    /** Nome do serviço (ex: "Corte + Barba") */
    String serviceName;

    /** Nome do profissional responsável */
    String professionalName;

    /** Data e hora do agendamento */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startAt;

    /** Código legível do agendamento (ex: "26020013") */
    String code;
}