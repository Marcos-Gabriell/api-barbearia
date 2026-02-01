package br.com.barbearia.apibarbearia.availability.repository;

import br.com.barbearia.apibarbearia.availability.entity.ScheduleBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, Long> {

    List<ScheduleBlock> findAllByUserId(Long userId);

}
