package br.com.barbearia.apibarbearia.schedule.repository;

import br.com.barbearia.apibarbearia.schedule.entity.TimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {
    List<TimeBlock> findByProfessionalIdAndDate(Long professionalId, LocalDate date);
}
