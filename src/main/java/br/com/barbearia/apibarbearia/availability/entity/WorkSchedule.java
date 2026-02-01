package br.com.barbearia.apibarbearia.availability.entity;

import lombok.*;
import javax.persistence.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_schedules")
public class WorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToMany(mappedBy = "workSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleDay> days = new ArrayList<>();

    public List<ScheduleDay> getDays() {
        if (days == null) {
            days = new ArrayList<>();
        }
        return days;
    }
}