package br.com.barbearia.apibarbearia.appointment.repository;

import br.com.barbearia.apibarbearia.appointment.entity.Appointment;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    Optional<Appointment> findById(Long id);

    Optional<Appointment> findByCode(String code);

    @Query("SELECT a.code FROM Appointment a WHERE a.code LIKE CONCAT(:prefix, '%') ORDER BY a.code DESC")
    List<String> findCodesByPrefix(@Param("prefix") String prefix);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.professionalUserId = :professionalId " +
            "AND a.status IN (:activeStatuses) " +
            "AND a.startAt < :endAt " +
            "AND a.endAt > :startAt")
    List<Appointment> findOverlapsForUpdate(
            @Param("professionalId") Long professionalId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("activeStatuses") List<AppointmentStatus> activeStatuses
    );

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.professionalUserId = :professionalId " +
            "AND a.status IN (:activeStatuses) " +
            "AND a.startAt >= :from AND a.startAt < :to " +
            "ORDER BY a.startAt ASC")
    List<Appointment> listDayAppointments(
            @Param("professionalId") Long professionalId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("activeStatuses") List<AppointmentStatus> activeStatuses
    );

    List<Appointment> findAllByStatusAndStartAtBetween(
            AppointmentStatus status,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Appointment> findAllByStatusAndStartAtBefore(
            AppointmentStatus status,
            LocalDateTime before
    );
}