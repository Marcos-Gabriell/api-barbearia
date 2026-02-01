package br.com.barbearia.apibarbearia.availability.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Embeddable;
import java.time.LocalTime;
import javax.persistence.Column;


@Embeddable
@Data
@NoArgsConstructor
public class TimeInterval {

    @Column(name = "start_time", nullable = false)
    private LocalTime start;

    @Column(name = "end_time", nullable = false)
    private LocalTime end;

    public TimeInterval(LocalTime start, LocalTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Horário final deve ser após o inicial");
        }
        this.start = start;
        this.end = end;
    }
}