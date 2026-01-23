package br.com.barbearia.apibarbearia.schedule.repository;

import br.com.barbearia.apibarbearia.schedule.entity.BreakInterval;
import br.com.barbearia.apibarbearia.schedule.entity.enums.DayOfWeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BreakIntervalRepository extends JpaRepository<BreakInterval, Long> {
    List<BreakInterval> findByProfessionalIdAndDayOfWeek(Long professionalId, DayOfWeekEnum dayOfWeek);
    List<BreakInterval> findByProfessionalId(Long professionalId);
}
