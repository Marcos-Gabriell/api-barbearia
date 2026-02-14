package br.com.barbearia.apibarbearia.availability.controller;

import br.com.barbearia.apibarbearia.availability.dto.BlockRequestDTO;
import br.com.barbearia.apibarbearia.availability.dto.ProfessionalDTO;
import br.com.barbearia.apibarbearia.availability.dto.ScheduleDTOs;
import br.com.barbearia.apibarbearia.availability.entity.ScheduleBlock;
import br.com.barbearia.apibarbearia.availability.service.AvailabilityService;
import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/availability")
@PreAuthorize("isAuthenticated()")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/professionals")
    public ResponseEntity<List<ProfessionalDTO>> listProfessionals() {
        Long requesterId = getUserId();
        String role = getRole();
        return ResponseEntity.ok(availabilityService.getManageableProfessionals(requesterId, role));
    }

    @GetMapping("/schedule/{targetUserId}")
    public ResponseEntity<ScheduleDTOs.ScheduleRequest> getSchedule(@PathVariable Long targetUserId) {
        Long requesterId = getUserId();
        String role = getRole();
        return ResponseEntity.ok(availabilityService.getSchedule(requesterId, role, targetUserId));
    }

    @PutMapping("/schedule/{targetUserId}")
    public ResponseEntity<Map<String, String>> updateSchedule(
            @PathVariable Long targetUserId,
            @RequestBody @Valid ScheduleDTOs.ScheduleRequest request
    ) {
        Long requesterId = getUserId();
        String role = getRole();
        availabilityService.updateRoutine(requesterId, role, targetUserId, request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Agenda semanal configurada com sucesso."));
    }

    @GetMapping("/blocks/{targetUserId}")
    public ResponseEntity<List<ScheduleBlock>> listBlocks(@PathVariable Long targetUserId) {
        Long requesterId = getUserId();
        String role = getRole();
        return ResponseEntity.ok(availabilityService.listBlocks(requesterId, role, targetUserId));
    }

    @PostMapping("/block")
    public ResponseEntity<Map<String, String>> createBlock(@RequestBody @Valid BlockRequestDTO request) {
        Long requesterId = getUserId();
        String role = getRole();
        availabilityService.createBlock(requesterId, role, request);
        return ResponseEntity.status(201).body(Collections.singletonMap("message", "Bloqueio criado com sucesso."));
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new BadRequestException("Sessão inválida.");
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Sessão inválida.");
        }
    }

    private String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BadRequestException("Sessão inválida.");
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("STAFF");
    }
}
