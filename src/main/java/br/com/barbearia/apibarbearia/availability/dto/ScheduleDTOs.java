package br.com.barbearia.apibarbearia.availability.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class ScheduleDTOs {

    public static class ScheduleRequest {
        @NotEmpty(message = "A lista de dias não pode estar vazia.")
        @Valid
        private List<DayConfig> days;

        public List<DayConfig> getDays() { return days; }
        public void setDays(List<DayConfig> days) { this.days = days; }
    }

    public static class DayConfig {
        @NotNull(message = "O dia da semana é obrigatório.")
        private DayOfWeek dayOfWeek;

        private boolean active;

        private LocalTime startTime;
        private LocalTime endTime;

        @Valid
        private List<IntervalDTO> breaks;

        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public List<IntervalDTO> getBreaks() { return breaks; }
        public void setBreaks(List<IntervalDTO> breaks) { this.breaks = breaks; }
    }

    public static class IntervalDTO {
        @NotNull(message = "O início do intervalo é obrigatório.")
        private LocalTime start;

        @NotNull(message = "O fim do intervalo é obrigatório.")
        private LocalTime end;

        public LocalTime getStart() { return start; }
        public void setStart(LocalTime start) { this.start = start; }

        public LocalTime getEnd() { return end; }
        public void setEnd(LocalTime end) { this.end = end; }
    }
}
