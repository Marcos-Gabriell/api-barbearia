package br.com.barbearia.apibarbearia.availability.entity;

import br.com.barbearia.apibarbearia.availability.entity.enums.BlockType;
import br.com.barbearia.apibarbearia.users.entity.User;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule_blocks")
public class ScheduleBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false) // ✅ ADICIONAR
    private LocalDate startDate;

    @Column(name = "end_date") // ✅ ADICIONAR
    private LocalDate endDate;

    @Column(name = "full_day", nullable = false) // ✅ ADICIONAR
    private boolean fullDay;

    @Column(name = "start_time") // ✅ ADICIONAR
    private LocalTime startTime;

    @Column(name = "end_time") // ✅ ADICIONAR
    private LocalTime endTime;

    @Column(length = 120)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockType type;

    public void validate() {
        if (startDate == null) {
            throw new IllegalArgumentException("Data inicial é obrigatória");
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Data final deve ser após a inicial");
        }

        if (!fullDay) {
            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException("Horários são obrigatórios para bloqueios parciais");
            }
            if (!startTime.isBefore(endTime)) {
                throw new IllegalArgumentException("Horário inicial deve ser antes do final");
            }
        }
    }

    public static ScheduleBlock createFullDayBlock(User user, LocalDate startDate, LocalDate endDate, String reason, BlockType type) {
        ScheduleBlock block = new ScheduleBlock();
        block.setUser(user);
        block.setStartDate(startDate);
        block.setEndDate(endDate != null ? endDate : startDate);
        block.setFullDay(true);
        block.setReason(reason);
        block.setType(type);
        return block;
    }

    public static ScheduleBlock createPartialBlock(User user, LocalDate date, LocalTime startTime, LocalTime endTime, String reason, BlockType type) {
        ScheduleBlock block = new ScheduleBlock();
        block.setUser(user);
        block.setStartDate(date);
        block.setEndDate(date);
        block.setFullDay(false);
        block.setStartTime(startTime);
        block.setEndTime(endTime);
        block.setReason(reason);
        block.setType(type);
        return block;
    }
}