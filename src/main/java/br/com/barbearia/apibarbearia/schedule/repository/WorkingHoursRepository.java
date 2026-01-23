package br.com.barbearia.apibarbearia.schedule.repository;

import br.com.barbearia.apibarbearia.schedule.entity.WorkingHours;
import br.com.barbearia.apibarbearia.schedule.entity.enums.DayOfWeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    Optional<WorkingHours> findByProfessionalIdAndDayOfWeek(Long professionalId, DayOfWeekEnum dayOfWeek);
    List<WorkingHours> findByProfessionalId(Long professionalId);
}
