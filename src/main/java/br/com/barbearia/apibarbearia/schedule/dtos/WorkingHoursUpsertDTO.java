package br.com.barbearia.apibarbearia.schedule.dtos;


import br.com.barbearia.apibarbearia.schedule.entity.enums.DayOfWeekEnum;

import java.time.LocalTime;

public class WorkingHoursUpsertDTO {
    public DayOfWeekEnum dayOfWeek;
    public LocalTime startTime;
    public LocalTime endTime;
    public Boolean active;
}
