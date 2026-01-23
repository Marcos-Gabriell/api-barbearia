package br.com.barbearia.apibarbearia.schedule.entity;



import br.com.barbearia.apibarbearia.schedule.entity.enums.DayOfWeekEnum;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(
        name = "working_hours",
        uniqueConstraints = @UniqueConstraint(columnNames = {"professional_id", "day_of_week"})
)
public class WorkingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="professional_id", nullable=false)
    private Long professionalId;

    @Enumerated(EnumType.STRING)
    @Column(name="day_of_week", nullable=false, length=3)
    private DayOfWeekEnum dayOfWeek;

    @Column(name="start_time", nullable=false)
    private LocalTime startTime;

    @Column(name="end_time", nullable=false)
    private LocalTime endTime;

    @Column(nullable=false)
    private Boolean active = true;

    public Long getId() { return id; }
    public Long getProfessionalId() { return professionalId; }
    public void setProfessionalId(Long professionalId) { this.professionalId = professionalId; }
    public DayOfWeekEnum getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeekEnum dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
