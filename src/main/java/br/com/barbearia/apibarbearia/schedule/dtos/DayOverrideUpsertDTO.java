package br.com.barbearia.apibarbearia.schedule.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

public class DayOverrideUpsertDTO {
    public LocalDate date;
    public Boolean closed;
    public LocalTime startTime;
    public LocalTime endTime;
    public String note;
}
