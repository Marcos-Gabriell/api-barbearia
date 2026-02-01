package br.com.barbearia.apibarbearia.availability.repository;


import br.com.barbearia.apibarbearia.availability.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    Optional<WorkSchedule> findByUserId(Long userId);
}