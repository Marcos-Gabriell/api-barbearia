package br.com.barbearia.apibarbearia.availability.controller;

import br.com.barbearia.apibarbearia.availability.dto.BlockRequestDTO;
import br.com.barbearia.apibarbearia.availability.dto.ScheduleDTOs;
import br.com.barbearia.apibarbearia.availability.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/availability")
@PreAuthorize("isAuthenticated()")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // ✅ FIX: ADICIONADO O ENDPOINT GET QUE FALTAVA
    @GetMapping("/schedule/{targetUserId}")
    public ResponseEntity<ScheduleDTOs.ScheduleRequest> getSchedule(@PathVariable Long targetUserId) {
        Long requesterId = getAuthenticatedUserId();
        String requesterRole = getAuthenticatedUserRole();

        // Chama o serviço de leitura que criamos anteriormente
        var schedule = availabilityService.getSchedule(requesterId, requesterRole, targetUserId);

        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/schedule/{targetUserId}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long targetUserId,
            @RequestBody @Valid ScheduleDTOs.ScheduleRequest request) {

        Long requesterId = getAuthenticatedUserId();
        String requesterRole = getAuthenticatedUserRole();

        availabilityService.updateRoutine(requesterId, requesterRole, targetUserId, request);

        return ResponseEntity.ok(Map.of(
                "message", "Agenda semanal configurada com sucesso."
        ));
    }

    @PostMapping("/block")
    public ResponseEntity<?> createBlock(@RequestBody @Valid BlockRequestDTO request) {

        Long requesterId = getAuthenticatedUserId();
        String requesterRole = getAuthenticatedUserRole();

        availabilityService.createBlock(requesterId, requesterRole, request);

        return ResponseEntity.status(201).body(Map.of(
                "message", "Bloqueio de agenda criado com sucesso."
        ));
    }

    // --- Helpers ---

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof br.com.barbearia.apibarbearia.users.entity.User) {
            return ((br.com.barbearia.apibarbearia.users.entity.User) auth.getPrincipal()).getId();
        }
        throw new RuntimeException("Usuário não identificado no contexto de segurança");
    }

    private String getAuthenticatedUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getAuthorities().isEmpty()) {
            String role = auth.getAuthorities().iterator().next().getAuthority();
            return role.replace("ROLE_", "");
        }
        return "STAFF";
    }
}