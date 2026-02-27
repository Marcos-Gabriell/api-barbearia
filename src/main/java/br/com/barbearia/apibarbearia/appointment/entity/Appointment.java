package br.com.barbearia.apibarbearia.appointment.entity;

import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentCancelReason;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    // ================== DADOS DO CLIENTE ==================
    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String clientEmail;

    @Column(nullable = false, length = 20)
    private String clientPhone;

    // ================== DADOS DO SERVIÇO ==================
    @Column(nullable = false)
    private Long serviceId;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private Integer durationMinutes;

    // ================== DADOS DO PROFISSIONAL ==================
    @Column(nullable = false)
    private Long professionalUserId;

    @Column(nullable = false)
    private String professionalName;

    @Column(nullable = false)
    private String professionalEmail;

    // ================== HORÁRIOS ==================
    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    // ================== STATUS ==================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    // ================== AUDITORIA DE CRIAÇÃO ==================
    private LocalDateTime createdAt;

    private Long createdByUserId;

    @Column(length = 20)
    private String createdByRole;

    @Column(length = 100)
    private String createdByUsername;

    @Column(length = 150)
    private String createdByEmail;

    // ================== AUDITORIA DE CONFIRMAÇÃO ==================
    private LocalDateTime confirmedAt;

    private Long confirmedByUserId;

    @Column(length = 20)
    private String confirmedByRole;

    @Column(length = 100)
    private String confirmedByUsername;

    @Column(length = 150)
    private String confirmedByEmail;

    // ================== AUDITORIA DE CANCELAMENTO ==================
    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AppointmentCancelReason cancelReason;

    @Column(length = 500)
    private String cancelMessage;

    private Long canceledByUserId;

    @Column(length = 20)
    private String canceledByRole;

    @Column(length = 100)
    private String canceledByUsername;

    @Column(length = 150)
    private String canceledByEmail;

    @Column(length = 20)
    private String cancelOrigin;  // "INTERNAL" ou "CLIENT"

    // ================== AUDITORIA DE NO-SHOW ==================
    private LocalDateTime noShowAt;

    private Long noShowByUserId;

    @Column(length = 20)
    private String noShowByRole;

    @Column(length = 100)
    private String noShowByUsername;

    // ================== ÚLTIMA ATUALIZAÇÃO ==================
    private LocalDateTime updatedAt;

    // ================== MÉTODOS AUXILIARES ==================

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isCancelable() {
        if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW) {
            return false;
        }
        return LocalDateTime.now().isBefore(startAt.minusMinutes(10));
    }

    public boolean isConfirmable() {
        if (status != AppointmentStatus.PENDING) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt.minusMinutes(10)) && now.isBefore(startAt.plusMinutes(10));
    }

    public boolean isNoShowMarkable() {
        if (status != AppointmentStatus.PENDING) {
            return false;
        }
        return LocalDateTime.now().isAfter(startAt);
    }

    public String getCreatedByDescription() {
        if (createdByUsername == null || createdByUsername.isEmpty()) {
            return createdByRole != null ? createdByRole : "Sistema";
        }
        return createdByUsername + " (" + createdByRole + ")";
    }

    public String getCanceledByDescription() {
        if (canceledAt == null) return null;
        if ("CLIENT".equals(cancelOrigin)) {
            return "Cliente via link";
        }
        if (canceledByUsername == null || canceledByUsername.isEmpty()) {
            return canceledByRole != null ? canceledByRole : "Sistema";
        }
        return canceledByUsername + " (" + canceledByRole + ")";
    }

    public String getConfirmedByDescription() {
        if (confirmedAt == null) return null;
        if (confirmedByUsername == null || confirmedByUsername.isEmpty()) {
            return confirmedByRole != null ? confirmedByRole : "Sistema";
        }
        return confirmedByUsername + " (" + confirmedByRole + ")";
    }

    public String getNoShowByDescription() {
        if (noShowAt == null) return null;
        if (noShowByUsername == null || noShowByUsername.isEmpty()) {
            return noShowByRole != null ? noShowByRole : "Sistema";
        }
        return noShowByUsername + " (" + noShowByRole + ")";
    }
}