package br.com.barbearia.apibarbearia.availability.entity;

import lombok.*;
import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule_days",
        uniqueConstraints = @UniqueConstraint(columnNames = {"work_schedule_id", "day_of_week"}))
public class ScheduleDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false) // ✅ ADICIONAR name
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "start_time") // ✅ ADICIONAR
    private LocalTime startTime;

    @Column(name = "end_time") // ✅ ADICIONAR
    private LocalTime endTime;

    @ElementCollection
    @CollectionTable(name = "schedule_day_breaks",
            joinColumns = @JoinColumn(name = "schedule_day_id"))
    private List<TimeInterval> breaks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_schedule_id", nullable = false)
    private WorkSchedule workSchedule;

    public boolean isWorkingAt(LocalTime time) {
        if (!active) return false;
        if (time.isBefore(startTime) || time.isAfter(endTime)) return false;

        return breaks.stream()
                .noneMatch(b -> !time.isBefore(b.getStart()) && !time.isAfter(b.getEnd()));
    }
    public List<TimeInterval> getBreaks() {
        if (breaks == null) {
            breaks = new ArrayList<>();
        }
        return breaks;
    }
}