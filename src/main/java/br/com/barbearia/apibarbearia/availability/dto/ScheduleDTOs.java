package br.com.barbearia.apibarbearia.availability.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotEmpty;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class ScheduleDTOs {

    @Data
    public static class ScheduleRequest {
        @NotEmpty(message = "A lista de dias não pode estar vazia.")
        @Valid
        private List<DayConfig> days;
    }

    @Data
    public static class DayConfig {
        @NotNull(message = "O dia da semana é obrigatório.")
        private DayOfWeek dayOfWeek;

        private boolean active;

        private LocalTime startTime;
        private LocalTime endTime;

        @Valid
        // Agora a lista usa o nome correto "IntervalDTO"
        private List<IntervalDTO> breaks;
    }

    @Data
    // RENOMEADO DE IntervalRequest PARA IntervalDTO
    // Assim bate com o código do AvailabilityService
    public static class IntervalDTO {
        @NotNull(message = "O início do intervalo é obrigatório.")
        private LocalTime start;

        @NotNull(message = "O fim do intervalo é obrigatório.")
        private LocalTime end;
    }
}