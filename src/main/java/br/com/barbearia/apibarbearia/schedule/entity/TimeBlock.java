package br.com.barbearia.apibarbearia.schedule.entity;

import br.com.barbearia.apibarbearia.schedule.entity.enums.BlockType;

import javax.persistence.*;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="time_block")
public class TimeBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="professional_id", nullable=false)
    private Long professionalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private BlockType type;

    private LocalDate date;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private String reason;

    public Long getId() { return id; }
    public Long getProfessionalId() { return professionalId; }
    public void setProfessionalId(Long professionalId) { this.professionalId = professionalId; }
    public BlockType getType() { return type; }
    public void setType(BlockType type) { this.type = type; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
