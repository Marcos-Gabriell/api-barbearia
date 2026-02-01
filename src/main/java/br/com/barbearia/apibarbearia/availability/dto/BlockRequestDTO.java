package br.com.barbearia.apibarbearia.availability.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BlockRequestDTO {
    private Long targetUserId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isFullDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
}