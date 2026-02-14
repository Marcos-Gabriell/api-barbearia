package br.com.barbearia.apibarbearia.availability.controller;

import br.com.barbearia.apibarbearia.availability.dto.BlockRequestDTO;
import br.com.barbearia.apibarbearia.availability.dto.ScheduleDTOs;
import br.com.barbearia.apibarbearia.availability.service.AvailabilityService;
import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@PreAuthorize("isAuthenticated()")
public class MyAvailabilityController {

    private final AvailabilityService availabilityService;

    public MyAvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/schedule")
    public ResponseEntity<ScheduleDTOs.ScheduleRequest> getMySchedule() {
        Long me = getAuthenticatedUserId();
        String role = getAuthenticatedUserRole();
        return ResponseEntity.ok(availabilityService.getSchedule(me, role, me));
    }

    @PutMapping("/schedule")
    public ResponseEntity<?> updateMySchedule(@RequestBody @Valid ScheduleDTOs.ScheduleRequest request) {
        Long me = getAuthenticatedUserId();
        String role = getAuthenticatedUserRole();
        availabilityService.updateRoutine(me, role, me, request);
        return ResponseEntity.ok(Map.of("message", "Agenda atualizada com sucesso."));
    }

    @GetMapping("/blocks")
    public ResponseEntity<?> listMyBlocks() {
        Long me = getAuthenticatedUserId();
        String role = getAuthenticatedUserRole();
        return ResponseEntity.ok(availabilityService.listBlocks(me, role, me));
    }

    @PostMapping("/blocks")
    public ResponseEntity<?> createMyBlock(@RequestBody @Valid BlockRequestDTO request) {
        Long me = getAuthenticatedUserId();
        String role = getAuthenticatedUserRole();

        request.setTargetUserId(me);

        availabilityService.createBlock(me, role, request);
        return ResponseEntity.status(201).body(Map.of("message", "Bloqueio criado com sucesso."));
    }

    private Long getAuthenticatedUserId() {
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

    private String getAuthenticatedUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BadRequestException("Sessão inválida.");

        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("STAFF");
    }
}
