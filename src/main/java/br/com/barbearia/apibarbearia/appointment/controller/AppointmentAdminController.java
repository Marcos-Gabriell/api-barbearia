package br.com.barbearia.apibarbearia.appointment.controller;

import br.com.barbearia.apibarbearia.appointment.dto.request.*;
import br.com.barbearia.apibarbearia.appointment.dto.response.AppointmentDetailResponse;
import br.com.barbearia.apibarbearia.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentAdminController {

    private final AppointmentService appointmentService;

    /**
     * Lista agendamentos com filtros e paginação.
     *
     * DEV/ADMIN: vê todos
     * STAFF: vê apenas seus próprios agendamentos
     */
    @GetMapping
    public ResponseEntity<Page<AppointmentDetailResponse>> list(
            AppointmentFilterRequest filter,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return ResponseEntity.ok(appointmentService.list(filter, requesterId, requesterRole));
    }

    /**
     * Busca detalhes de um agendamento pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDetailResponse> getDetails(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return ResponseEntity.ok(appointmentService.getDetails(id, requesterId, requesterRole));
    }

    /**
     * Busca detalhes de um agendamento pelo código único.
     * Ex: GET /api/appointments/code/2502-0001
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<AppointmentDetailResponse> getDetailsByCode(
            @PathVariable String code,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return ResponseEntity.ok(appointmentService.getDetailsByCode(code, requesterId, requesterRole));
    }

    /**
     * Cria um novo agendamento interno (via balcão/admin).
     * Retorna o agendamento criado com todos os detalhes.
     */
    @PostMapping
    public ResponseEntity<AppointmentDetailResponse> createInternal(
            @Valid @RequestBody CreateAppointmentInternalRequest req,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        AppointmentDetailResponse response = appointmentService.createInternal(req, requesterId, requesterRole);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancela um agendamento.
     *
     * Regras:
     * - Só pode cancelar até 10 minutos antes do horário
     * - DEV/ADMIN: pode cancelar qualquer agendamento
     * - STAFF: só pode cancelar seus próprios agendamentos
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) CancelAppointmentInternalRequest req,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.cancelInternal(id, req, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }

    /**
     * Confirma a chegada do cliente.
     *
     * Regras:
     * - Só pode confirmar entre 10 minutos ANTES e 10 minutos DEPOIS do horário
     * - Após 10 minutos do horário, o agendamento é marcado como NO_SHOW automaticamente
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmArrival(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.confirmArrival(id, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca manualmente como no-show (não compareceu).
     *
     * Regras:
     * - Só pode marcar após o horário do agendamento
     * - Não pode marcar se já foi confirmado
     */
    @PostMapping("/{id}/no-show")
    public ResponseEntity<?> markNoShow(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.markNoShow(id, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }
}