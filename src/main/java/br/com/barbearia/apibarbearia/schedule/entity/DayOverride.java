package br.com.barbearia.apibarbearia.schedule.entity;

import javax.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.*;
import java.time.LocalTime;


@Entity
@Table(
        name="day_override",
        uniqueConstraints = @UniqueConstraint(columnNames = {"professional_id", "date"})
)
public class DayOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="professional_id", nullable=false)
    private Long professionalId;

    @Column(nullable=false)
    private LocalDate date;

    @Column(nullable=false)
    private Boolean closed = false;

    private LocalTime startTime;
    private LocalTime endTime;

    private String note;

    public Long getId() { return id; }
    public Long getProfessionalId() { return professionalId; }
    public void setProfessionalId(Long professionalId) { this.professionalId = professionalId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Boolean getClosed() { return closed; }
    public void setClosed(Boolean closed) { this.closed = closed; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
