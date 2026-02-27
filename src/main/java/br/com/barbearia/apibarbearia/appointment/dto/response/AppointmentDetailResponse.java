package br.com.barbearia.apibarbearia.appointment.dto.response;

import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentCancelReason;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentDetailResponse {

    private Long id;
    private String code;

    // Dados do cliente
    private String clientName;
    private String clientEmail;
    private String clientPhone;

    // Dados do serviço
    private Long serviceId;
    private String serviceName;
    private Integer durationMinutes;

    // Dados do profissional
    private Long professionalUserId;
    private String professionalName;
    private String professionalEmail;

    // Horários
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // Status
    private AppointmentStatus status;

    // Auditoria de criação
    private LocalDateTime createdAt;
    private Long createdByUserId;
    private String createdByRole;
    private String createdByUsername;
    private String createdByEmail;
    private String createdByDescription;

    // Auditoria de confirmação
    private LocalDateTime confirmedAt;
    private Long confirmedByUserId;
    private String confirmedByRole;
    private String confirmedByUsername;
    private String confirmedByEmail;
    private String confirmedByDescription;

    // Auditoria de cancelamento
    private LocalDateTime canceledAt;
    private AppointmentCancelReason cancelReason;
    private String cancelMessage;
    private Long canceledByUserId;
    private String canceledByRole;
    private String canceledByUsername;
    private String canceledByEmail;
    private String cancelOrigin;
    private String canceledByDescription;

    // Auditoria de no-show
    private LocalDateTime noShowAt;
    private Long noShowByUserId;
    private String noShowByRole;
    private String noShowByUsername;
    private String noShowByDescription;

    // Última atualização
    private LocalDateTime updatedAt;

    // Flags de ações
    private Boolean canCancel;
    private Boolean canConfirm;
    private Boolean canMarkNoShow;
}