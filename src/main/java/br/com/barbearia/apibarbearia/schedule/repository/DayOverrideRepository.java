package br.com.barbearia.apibarbearia.schedule.repository;

import br.com.barbearia.apibarbearia.schedule.entity.DayOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayOverrideRepository extends JpaRepository<DayOverride, Long> {
    Optional<DayOverride> findByProfessionalIdAndDate(Long professionalId, LocalDate date);
    List<DayOverride> findByProfessionalIdAndDateBetween(Long professionalId, LocalDate from, LocalDate to);
}
