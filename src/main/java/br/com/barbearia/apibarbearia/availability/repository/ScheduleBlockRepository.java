package br.com.barbearia.apibarbearia.availability.repository;

import br.com.barbearia.apibarbearia.availability.entity.ScheduleBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, Long> {
    List<ScheduleBlock> findAllByUser_Id(Long userId);
}
