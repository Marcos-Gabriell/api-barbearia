package br.com.barbearia.apibarbearia.availability.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public class BlockRequestDTO {

    @NotNull(message = "targetUserId é obrigatório.")
    private Long targetUserId;

    @NotNull(message = "startDate é obrigatório.")
    private LocalDate startDate;

    @NotNull(message = "endDate é obrigatório.")
    private LocalDate endDate;

    private boolean fullDay;

    private LocalTime startTime;
    private LocalTime endTime;

    @Size(max = 120, message = "Motivo muito longo (máx 120 caracteres).")
    private String reason;

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isFullDay() { return fullDay; }
    public void setFullDay(boolean fullDay) { this.fullDay = fullDay; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
