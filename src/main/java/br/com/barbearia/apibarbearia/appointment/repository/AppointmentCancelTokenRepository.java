package br.com.barbearia.apibarbearia.appointment.repository;

import br.com.barbearia.apibarbearia.appointment.entity.AppointmentCancelToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentCancelTokenRepository extends JpaRepository<AppointmentCancelToken, Long> {
    Optional<AppointmentCancelToken> findByToken(String token);
    Optional<AppointmentCancelToken> findByAppointmentId(Long appointmentId);
}
