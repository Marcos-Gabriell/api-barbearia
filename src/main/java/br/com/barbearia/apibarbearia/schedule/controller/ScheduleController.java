package br.com.barbearia.apibarbearia.schedule.controller;

import br.com.barbearia.apibarbearia.notification.email.schedule.ScheduleEmailNotificationService;
import br.com.barbearia.apibarbearia.schedule.dtos.*;
import br.com.barbearia.apibarbearia.schedule.entity.*;
import br.com.barbearia.apibarbearia.schedule.service.ScheduleService;
import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleEmailNotificationService scheduleEmail;
    private final UserRepository userRepository;

    public ScheduleController(ScheduleService scheduleService,
                              ScheduleEmailNotificationService scheduleEmail,
                              UserRepository userRepository) {
        this.scheduleService = scheduleService;
        this.scheduleEmail = scheduleEmail;
        this.userRepository = userRepository;
    }

    @PutMapping("/professionals/{professionalId}/working-hours")
    public ResponseEntity<WorkingHours> upsertWorkingHours(
            @PathVariable Long professionalId,
            @RequestBody WorkingHoursUpsertDTO dto
    ) {
        WorkingHours saved = scheduleService.upsertWorkingHours(professionalId, dto);

        String updatedBy = currentActorDisplay();
        String professionalName = professionalLabel(professionalId); // TODO: buscar nome real do profissional

        notifyAdminsAndStaffWorkingHours(professionalName, dto, updatedBy);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/professionals/{professionalId}/working-hours")
    public ResponseEntity<List<WorkingHours>> listWorkingHours(@PathVariable Long professionalId) {
        return ResponseEntity.ok(scheduleService.listWorkingHours(professionalId));
    }

    @PostMapping("/professionals/{professionalId}/breaks")
    public ResponseEntity<BreakInterval> createBreak(
            @PathVariable Long professionalId,
            @RequestBody BreakCreateDTO dto
    ) {
        BreakInterval saved = scheduleService.createBreak(professionalId, dto);

        String createdBy = currentActorDisplay();
        String professionalName = professionalLabel(professionalId); // TODO

        notifyAdminsAndStaffBreakCreated(professionalName, dto, createdBy);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/professionals/{professionalId}/breaks")
    public ResponseEntity<List<BreakInterval>> listBreaks(@PathVariable Long professionalId) {
        return ResponseEntity.ok(scheduleService.listBreaks(professionalId));
    }

    @DeleteMapping("/professionals/{professionalId}/breaks/{breakId}")
    public ResponseEntity<Void> deleteBreak(
            @PathVariable Long professionalId,
            @PathVariable Long breakId
    ) {
        BreakInterval snapshot = scheduleService.findBreakByIdAndProfessional(breakId, professionalId)
                .orElse(null);

        scheduleService.deleteBreak(professionalId, breakId);

        if (snapshot != null) {
            String deletedBy = currentActorDisplay();
            String professionalName = professionalLabel(professionalId); // TODO
            notifyAdminsAndStaffBreakDeleted(professionalName, snapshot, deletedBy);
        }

        return ResponseEntity.noContent().build();
    }


    @PutMapping("/professionals/{professionalId}/overrides")
    public ResponseEntity<DayOverride> upsertOverride(
            @PathVariable Long professionalId,
            @RequestBody DayOverrideUpsertDTO dto
    ) {
        DayOverride saved = scheduleService.upsertOverride(professionalId, dto);

        String updatedBy = currentActorDisplay();
        String professionalName = professionalLabel(professionalId); // TODO

        notifyAdminsAndStaffOverride(professionalName, dto, updatedBy);

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/professionals/{professionalId}/overrides/{overrideId}")
    public ResponseEntity<Void> deleteOverride(
            @PathVariable Long professionalId,
            @PathVariable Long overrideId
    ) {
        DayOverride snapshot = scheduleService.findOverrideByIdAndProfessional(overrideId, professionalId)
                .orElse(null);

        scheduleService.deleteOverride(professionalId, overrideId);

        if (snapshot != null) {
            String updatedBy = currentActorDisplay();
            String professionalName = professionalLabel(professionalId); // TODO

            // manda e-mail como "updated" informando remoção (padrão simples)
            DayOverrideUpsertDTO dto = new DayOverrideUpsertDTO();
            dto.date = snapshot.getDate();
            dto.closed = true; // sinaliza mudança forte
            dto.startTime = null;
            dto.endTime = null;
            dto.note = "Override removido";

            notifyAdminsAndStaffOverride(professionalName, dto, updatedBy);
        }

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/professionals/{professionalId}/blocks")
    public ResponseEntity<TimeBlock> createBlock(
            @PathVariable Long professionalId,
            @RequestBody BlockCreateDTO dto
    ) {
        return ResponseEntity.ok(scheduleService.createBlock(professionalId, dto));
    }

    @DeleteMapping("/professionals/{professionalId}/blocks/{blockId}")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable Long professionalId,
            @PathVariable Long blockId
    ) {
        scheduleService.deleteBlock(professionalId, blockId);
        return ResponseEntity.noContent().build();
    }

    private void notifyAdminsAndStaffWorkingHours(String professionalName,
                                                  WorkingHoursUpsertDTO dto,
                                                  String updatedBy) {
        List<User> recipients = adminsAndStaffActive();

        for (User u : recipients) {
            try {
                scheduleEmail.sendWorkingHoursUpdated(
                        u.getEmail(),
                        safeUserName(u),
                        professionalName,
                        dto.dayOfWeek.name(),
                        String.valueOf(dto.startTime),
                        String.valueOf(dto.endTime),
                        updatedBy
                );
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyAdminsAndStaffBreakCreated(String professionalName,
                                                  BreakCreateDTO dto,
                                                  String createdBy) {
        List<User> recipients = adminsAndStaffActive();

        for (User u : recipients) {
            try {
                scheduleEmail.sendBreakCreated(
                        u.getEmail(),
                        safeUserName(u),
                        professionalName,
                        dto.dayOfWeek.name(),
                        String.valueOf(dto.startTime),
                        String.valueOf(dto.endTime),
                        createdBy
                );
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyAdminsAndStaffBreakDeleted(String professionalName,
                                                  BreakInterval snapshot,
                                                  String deletedBy) {
        List<User> recipients = adminsAndStaffActive();

        for (User u : recipients) {
            try {
                scheduleEmail.sendBreakDeleted(
                        u.getEmail(),
                        safeUserName(u),
                        professionalName,
                        snapshot.getDayOfWeek().name(),
                        String.valueOf(snapshot.getStartTime()),
                        String.valueOf(snapshot.getEndTime()),
                        deletedBy
                );
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyAdminsAndStaffOverride(String professionalName,
                                              DayOverrideUpsertDTO dto,
                                              String updatedBy) {
        List<User> recipients = adminsAndStaffActive();

        boolean closed = dto.closed != null && dto.closed;
        String status = closed ? "FECHADO" : "ABERTO";
        String timeRange = closed ? "-" : (dto.startTime + " - " + dto.endTime);

        for (User u : recipients) {
            try {
                scheduleEmail.sendDayOverrideUpdated(
                        u.getEmail(),
                        safeUserName(u),
                        professionalName,
                        String.valueOf(dto.date),
                        status,
                        timeRange,
                        updatedBy
                );
            } catch (Exception ignored) {
            }
        }
    }

    private List<User> adminsAndStaffActive() {
        List<User> all = new ArrayList<>();
        all.addAll(userRepository.findAllByRoleAndActiveTrue(Role.ADMIN));
        all.addAll(userRepository.findAllByRoleAndActiveTrue(Role.STAFF));
        all.addAll(userRepository.findAllByRoleAndActiveTrue(Role.DEV));
        return all;
    }

    private String currentActorEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return "system@local";
        return auth.getName().toLowerCase(Locale.ROOT);
    }

    private String currentActorDisplay() {
        String email = currentActorEmail();
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isPresent()) {
            String name = safeUserName(u.get());
            return name + " (" + email + ")";
        }
        return email;
    }

    private String safeUserName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) return user.getEmail();
        return name;
    }

    private String professionalLabel(Long professionalId) {
        return "Profissional #" + professionalId;
    }
}
