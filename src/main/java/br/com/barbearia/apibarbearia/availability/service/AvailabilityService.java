package br.com.barbearia.apibarbearia.availability.service;

import br.com.barbearia.apibarbearia.availability.common.exception.ForbiddenException;
import br.com.barbearia.apibarbearia.availability.dto.BlockRequestDTO;
import br.com.barbearia.apibarbearia.availability.dto.ScheduleDTOs;
import br.com.barbearia.apibarbearia.availability.entity.*;
import br.com.barbearia.apibarbearia.availability.repository.ScheduleBlockRepository;
import br.com.barbearia.apibarbearia.availability.repository.WorkScheduleRepository;
import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.ConflictException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.availability.entity.enums.BlockType;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final WorkScheduleRepository scheduleRepo;
    private final ScheduleBlockRepository blockRepo;
    private final UserRepository userRepository;

    public AvailabilityService(
            WorkScheduleRepository scheduleRepo,
            ScheduleBlockRepository blockRepo,
            UserRepository userRepository
    ) {
        this.scheduleRepo = scheduleRepo;
        this.blockRepo = blockRepo;
        this.userRepository = userRepository;
    }

    // =========================================================
    // AGENDA SEMANAL (ROTINA)
    // =========================================================

    /**
     * Cria agenda padrão (Seg-Sáb 09:00-18:00, Dom inativo).
     * Use no create user (UserService) e/ou manualmente.
     * NÃO será chamada no GET para evitar "GET que grava".
     */
    @Transactional
    public void createDefaultSchedule(Long userId) {
        if (userId == null) {
            throw new BadRequestException("Usuário inválido.");
        }

        // ✅ Verificar se já existe
        if (scheduleRepo.findByUserId(userId).isPresent()) {
            return; // Já existe, não cria
        }

        // ✅ Criar WorkSchedule
        WorkSchedule schedule = new WorkSchedule();
        schedule.setUserId(userId);
        schedule.setDays(new ArrayList<>());

        // ✅ Criar os 7 dias da semana
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            ScheduleDay day = new ScheduleDay();
            day.setDayOfWeek(dayOfWeek);
            day.setWorkSchedule(schedule);
            day.setBreaks(new ArrayList<>());

            if (dayOfWeek == DayOfWeek.SUNDAY) {
                // ✅ DOMINGO: INATIVO
                day.setActive(false);
                day.setStartTime(null);
                day.setEndTime(null);
            } else {
                // ✅ SEGUNDA A SÁBADO: 08:00 - 18:00
                day.setActive(true);
                day.setStartTime(LocalTime.of(8, 0));  // ✅ CORRIGIDO: 08:00 (era 09:00)
                day.setEndTime(LocalTime.of(18, 0));
            }

            schedule.getDays().add(day);
        }

        // ✅ Salvar tudo de uma vez (cascade vai salvar os days também)
        scheduleRepo.save(schedule);
    }
    /**
     * GET da agenda:
     * - Se não existir, retorna days vazio (não cria nada).
     * - Se você preferir 404, tem a opção comentada.
     */
    @Transactional(readOnly = true)
    public ScheduleDTOs.ScheduleRequest getSchedule(Long requesterId, String requesterRole, Long targetUserId) {
        validateHierarchy(requesterId, requesterRole, targetUserId);

        Optional<WorkSchedule> opt = scheduleRepo.findByUserId(targetUserId);

        // OPÇÃO 1: retornar vazio (seu pedido)
        if (opt.isEmpty()) {
            ScheduleDTOs.ScheduleRequest empty = new ScheduleDTOs.ScheduleRequest();
            empty.setDays(new ArrayList<>());
            return empty;
        }

        // OPÇÃO 2 (alternativa): retornar 404
        // WorkSchedule schedule = opt.orElseThrow(() -> new NotFoundException("Agenda não encontrada."));

        WorkSchedule schedule = opt.get();
        if (schedule.getDays() == null) schedule.setDays(new ArrayList<>());

        ScheduleDTOs.ScheduleRequest response = new ScheduleDTOs.ScheduleRequest();
        response.setDays(schedule.getDays().stream().map(this::mapDayToDto).collect(Collectors.toList()));
        return response;
    }

    @Transactional
    public void updateRoutine(Long requesterId, String requesterRole, Long targetUserId, ScheduleDTOs.ScheduleRequest dto) {
        validateHierarchy(requesterId, requesterRole, targetUserId);

        if (dto == null) throw new BadRequestException("Payload inválido.");
        validateDaysPayload(dto.getDays());
        validateTimeLogic(dto.getDays());

        WorkSchedule schedule = scheduleRepo.findByUserId(targetUserId)
                .orElseGet(() -> {
                    // ✅ CORRIGIDO: Sem builder
                    WorkSchedule s = new WorkSchedule();
                    s.setUserId(targetUserId);
                    s.setDays(new ArrayList<>());
                    return s;
                });

        if (schedule.getDays() == null) schedule.setDays(new ArrayList<>());
        schedule.getDays().clear();

        List<ScheduleDay> newDays = dto.getDays().stream()
                .map(dayDto -> mapDtoToDay(dayDto, schedule))
                .collect(Collectors.toList());

        schedule.getDays().addAll(newDays);
        scheduleRepo.save(schedule);
    }

    @Transactional
    public void createBlock(Long requesterId, String requesterRole, BlockRequestDTO dto) {
        if (dto == null) throw new BadRequestException("Payload inválido.");
        if (dto.getTargetUserId() == null) throw new BadRequestException("targetUserId é obrigatório.");

        validateHierarchy(requesterId, requesterRole, dto.getTargetUserId());
        validateBlock(dto);

        // ✅ BUSCAR O USUÁRIO PRIMEIRO
        User user = userRepository.findById(dto.getTargetUserId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        ensureNoBlockOverlap(
                dto.getTargetUserId(), // Mantém como está por enquanto
                dto.getStartDate(),
                dto.getEndDate(),
                dto.isFullDay(),
                dto.getStartTime(),
                dto.getEndTime()
        );

        // ✅ CORRIGIDO: Usar factory methods
        ScheduleBlock block;

        if (dto.isFullDay()) {
            block = ScheduleBlock.createFullDayBlock(
                    user,
                    dto.getStartDate(),
                    dto.getEndDate(),
                    dto.getReason(),
                    BlockType.BLOCKED
            );
        } else {
            block = ScheduleBlock.createPartialBlock(
                    user,
                    dto.getStartDate(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    dto.getReason(),
                    BlockType.BLOCKED
            );
        }

        blockRepo.save(block);
    }

    @Transactional(readOnly = true)
    public List<ScheduleBlock> listBlocks(Long requesterId, String requesterRole, Long targetUserId) {
        validateHierarchy(requesterId, requesterRole, targetUserId);
        return blockRepo.findAllByUserId(targetUserId);
    }


    private ScheduleDTOs.DayConfig mapDayToDto(ScheduleDay day) {
        ScheduleDTOs.DayConfig d = new ScheduleDTOs.DayConfig();
        d.setDayOfWeek(day.getDayOfWeek());
        d.setActive(day.isActive());
        d.setStartTime(day.getStartTime());
        d.setEndTime(day.getEndTime());

        // ✅ MELHORADO: Usar getBreaks() que já garante não-null
        List<TimeInterval> breaks = day.getBreaks(); // já retorna ArrayList se null

        d.setBreaks(breaks.stream().map(b -> {
            ScheduleDTOs.IntervalDTO i = new ScheduleDTOs.IntervalDTO();
            i.setStart(b.getStart());
            i.setEnd(b.getEnd());
            return i;
        }).collect(Collectors.toList()));

        return d;
    }

    private ScheduleDay mapDtoToDay(ScheduleDTOs.DayConfig dayDto, WorkSchedule schedule) {
        ScheduleDay day = new ScheduleDay();
        day.setDayOfWeek(dayDto.getDayOfWeek());
        day.setActive(dayDto.isActive());
        day.setWorkSchedule(schedule);
        day.setBreaks(new ArrayList<>()); // ✅ Sempre inicializar

        if (dayDto.isActive()) {
            day.setStartTime(dayDto.getStartTime());
            day.setEndTime(dayDto.getEndTime());

            if (dayDto.getBreaks() != null) {
                day.setBreaks(dayDto.getBreaks().stream()
                        .map(b -> new TimeInterval(b.getStart(), b.getEnd()))
                        .collect(Collectors.toList()));
            }
        } else {
            day.setStartTime(null);
            day.setEndTime(null);
            // breaks já está vazio
        }

        return day;
    }

    // =========================================================
    // REGRAS DE ACESSO (HIERARQUIA)
    // =========================================================

    private void validateHierarchy(Long reqId, String reqRole, Long targetId) {
        if (reqId == null) throw new BadRequestException("Sessão inválida.");
        if (targetId == null) throw new BadRequestException("Usuário alvo inválido.");

        // self
        if (reqId.equals(targetId)) return;

        // DEV pode tudo
        if ("DEV".equalsIgnoreCase(reqRole)) return;

        // STAFF não mexe em outros
        if ("STAFF".equalsIgnoreCase(reqRole)) {
            throw new ForbiddenException("STAFF não pode gerenciar agenda de outros usuários.");
        }

        // ADMIN/ADM: não pode mexer em ADMIN/DEV
        if ("ADMIN".equalsIgnoreCase(reqRole) || "ADM".equalsIgnoreCase(reqRole)) {
            User targetUser = userRepository.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("Usuário alvo não encontrado."));

            String targetRole = targetUser.getRole().name();

            if ("ADMIN".equalsIgnoreCase(targetRole) || "ADM".equalsIgnoreCase(targetRole) || "DEV".equalsIgnoreCase(targetRole)) {
                throw new ForbiddenException("ADMIN não pode gerenciar outro ADMIN ou DEV.");
            }
            return;
        }

        throw new ForbiddenException("Role inválida.");
    }

    // =========================================================
    // VALIDAÇÕES - ROTINA
    // =========================================================

    private void validateDaysPayload(List<ScheduleDTOs.DayConfig> days) {
        if (days == null || days.isEmpty()) {
            throw new BadRequestException("Configuração de dias é obrigatória.");
        }
        if (days.size() != 7) {
            throw new BadRequestException("A agenda deve conter exatamente 7 dias (Segunda a Domingo).");
        }

        long unique = days.stream()
                .map(ScheduleDTOs.DayConfig::getDayOfWeek)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        if (unique != 7) {
            throw new BadRequestException("A agenda deve conter os 7 dias sem repetição.");
        }
    }

    private void validateTimeLogic(List<ScheduleDTOs.DayConfig> days) {
        for (ScheduleDTOs.DayConfig day : days) {
            if (day.getDayOfWeek() == null) {
                throw new BadRequestException("Dia da semana inválido.");
            }

            if (!day.isActive()) {
                // inativo: não exige horários
                continue;
            }

            if (day.getStartTime() == null || day.getEndTime() == null) {
                throw new BadRequestException("Dia ativo (" + day.getDayOfWeek() + ") deve ter horário de início e fim.");
            }
            if (!day.getStartTime().isBefore(day.getEndTime())) {
                throw new BadRequestException("Horário inválido em " + day.getDayOfWeek() + ": início deve ser antes do fim.");
            }

            if (day.getBreaks() == null || day.getBreaks().isEmpty()) continue;

            List<ScheduleDTOs.IntervalDTO> sorted = day.getBreaks().stream()
                    .sorted(Comparator.comparing(ScheduleDTOs.IntervalDTO::getStart, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            LocalTime lastEnd = null;

            for (ScheduleDTOs.IntervalDTO interval : sorted) {
                if (interval.getStart() == null || interval.getEnd() == null) {
                    throw new BadRequestException("Pausa deve ter início e fim (" + day.getDayOfWeek() + ").");
                }

                LocalTime s = interval.getStart();
                LocalTime e = interval.getEnd();

                if (!s.isBefore(e)) {
                    throw new BadRequestException("Pausa inválida em " + day.getDayOfWeek() + ": início deve ser antes do fim.");
                }

                if (s.isBefore(day.getStartTime()) || e.isAfter(day.getEndTime())) {
                    throw new BadRequestException("Pausa deve estar dentro do expediente em " + day.getDayOfWeek() + ".");
                }

                if (lastEnd != null && s.isBefore(lastEnd)) {
                    throw new BadRequestException("Conflito de pausas em " + day.getDayOfWeek() + ": intervalos não podem se sobrepor.");
                }

                lastEnd = e;
            }
        }
    }

    // =========================================================
    // VALIDAÇÕES - BLOQUEIO
    // =========================================================

    private void validateBlock(BlockRequestDTO dto) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new BadRequestException("Data inicial e final são obrigatórias.");
        }
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("A data final deve ser posterior ou igual à data inicial.");
        }

        if (dto.isFullDay()) {
            dto.setStartTime(null);
            dto.setEndTime(null);
        } else {
            if (dto.getStartTime() == null || dto.getEndTime() == null) {
                throw new BadRequestException("Para bloqueios parciais, horário de início e fim são obrigatórios.");
            }
            if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                throw new BadRequestException("Horário de início do bloqueio deve ser antes do fim.");
            }

            // parcial: simples, 1 dia
            if (!dto.getStartDate().equals(dto.getEndDate())) {
                throw new BadRequestException("Bloqueio parcial deve ser em um único dia (startDate = endDate).");
            }
        }

        if (dto.getReason() != null && dto.getReason().length() > 120) {
            throw new BadRequestException("Motivo muito longo (máx 120 caracteres).");
        }
    }

    private void ensureNoBlockOverlap(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            boolean fullDay,
            LocalTime startTime,
            LocalTime endTime
    ) {
        List<ScheduleBlock> existing = blockRepo.findAllByUserId(userId);
        if (existing == null || existing.isEmpty()) return;

        for (ScheduleBlock b : existing) {
            if (b.getStartDate() == null || b.getEndDate() == null) continue;

            boolean dateOverlap = !(endDate.isBefore(b.getStartDate()) || startDate.isAfter(b.getEndDate()));
            if (!dateOverlap) continue;

            if (fullDay) {
                throw new ConflictException("Conflito: já existe bloqueio nesse período.");
            }

            if (b.isFullDay()) { // ✅ CORRIGIDO: Usar isFullDay() gerado pelo Lombok
                throw new ConflictException("Conflito: já existe bloqueio de dia inteiro nesse período.");
            }

            // ambos parciais: valida mesmo dia (parcial só 1 dia)
            if (startDate.equals(b.getStartDate()) && endDate.equals(b.getEndDate())) {
                LocalTime ns = startTime;
                LocalTime ne = endTime;
                LocalTime es = b.getStartTime();
                LocalTime ee = b.getEndTime();

                if (ns != null && ne != null && es != null && ee != null) {
                    boolean timeOverlap = ns.isBefore(ee) && ne.isAfter(es);
                    if (timeOverlap) {
                        throw new ConflictException("Conflito: já existe bloqueio nesse horário.");
                    }
                }
            } else {
                throw new ConflictException("Conflito: já existe bloqueio no período selecionado.");
            }
        }
    }
}