package br.com.barbearia.apibarbearia.availability.service;

import br.com.barbearia.apibarbearia.availability.common.exception.ForbiddenException;
import br.com.barbearia.apibarbearia.availability.dto.BlockRequestDTO;
import br.com.barbearia.apibarbearia.availability.dto.ProfessionalDTO;
import br.com.barbearia.apibarbearia.availability.dto.ScheduleDTOs;
import br.com.barbearia.apibarbearia.availability.entity.ScheduleBlock;
import br.com.barbearia.apibarbearia.availability.entity.ScheduleDay;
import br.com.barbearia.apibarbearia.availability.entity.TimeInterval;
import br.com.barbearia.apibarbearia.availability.entity.WorkSchedule;
import br.com.barbearia.apibarbearia.availability.entity.enums.BlockType;
import br.com.barbearia.apibarbearia.availability.repository.ScheduleBlockRepository;
import br.com.barbearia.apibarbearia.availability.repository.WorkScheduleRepository;
import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.ConflictException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
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

    @Transactional(readOnly = true)
    public List<ProfessionalDTO> getManageableProfessionals(Long requesterId, String requesterRole) {
        if (requesterId == null) throw new BadRequestException("Sessão inválida.");

        String role = normalizeRole(requesterRole);
        List<User> users = userRepository.findAll();

        // DEV vê TODOS
        if ("DEV".equals(role)) {
            return users.stream()
                    .map(this::toProfessional)
                    .sorted(sortActivesFirstByName())
                    .collect(Collectors.toList());
        }

        // ADMIN vê apenas STAFF (exceto ele mesmo)
        if ("ADMIN".equals(role) || "ADM".equals(role)) {
            return users.stream()
                    .filter(u -> !Objects.equals(u.getId(), requesterId))
                    .filter(u -> "STAFF".equalsIgnoreCase(u.getRole().name()))
                    .map(this::toProfessional)
                    .sorted(sortActivesFirstByName())
                    .collect(Collectors.toList());
        }

        // STAFF não vê ninguém
        if ("STAFF".equals(role)) return Collections.emptyList();

        throw new ForbiddenException("Role inválida.");
    }

    @Transactional(readOnly = true)
    public ScheduleDTOs.ScheduleRequest getSchedule(Long requesterId, String requesterRole, Long targetUserId) {
        validateHierarchy(requesterId, requesterRole, targetUserId);

        WorkSchedule schedule = scheduleRepo.findByUserId(targetUserId).orElse(null);

        // Se não existe, cria agenda padrão primeiro
        if (schedule == null) {
            createDefaultSchedule(targetUserId);
            schedule = scheduleRepo.findByUserId(targetUserId).orElse(null);
        }

        if (schedule == null) {
            ScheduleDTOs.ScheduleRequest empty = new ScheduleDTOs.ScheduleRequest();
            empty.setDays(new ArrayList<>());
            return empty;
        }

        ScheduleDTOs.ScheduleRequest res = new ScheduleDTOs.ScheduleRequest();
        res.setDays(schedule.getDays().stream().map(this::mapDayToDto).collect(Collectors.toList()));
        return res;
    }

    @Transactional
    public void updateRoutine(Long requesterId, String requesterRole, Long targetUserId, ScheduleDTOs.ScheduleRequest dto) {
        validateHierarchy(requesterId, requesterRole, targetUserId);

        if (dto == null) throw new BadRequestException("Payload inválido.");
        validateDaysPayload(dto.getDays());
        validateTimeLogic(dto.getDays());

        WorkSchedule schedule = scheduleRepo.findByUserId(targetUserId).orElse(null);

        if (schedule == null) {
            schedule = new WorkSchedule();
            schedule.setUserId(targetUserId);
            schedule.setDays(new ArrayList<>());
        } else {
            // ✅ LIMPAR dias existentes ANTES de adicionar novos (evita duplicação)
            schedule.getDays().clear();
            scheduleRepo.flush(); // força a execução do DELETE antes do INSERT
        }

        // ✅ Mapear e adicionar os 7 dias
        for (ScheduleDTOs.DayConfig dayDto : dto.getDays()) {
            ScheduleDay day = mapDtoToDay(dayDto, schedule);
            schedule.getDays().add(day);
        }

        scheduleRepo.save(schedule);
    }

    @Transactional
    public void createBlock(Long requesterId, String requesterRole, BlockRequestDTO dto) {
        if (dto == null) throw new BadRequestException("Payload inválido.");
        if (dto.getTargetUserId() == null) throw new BadRequestException("targetUserId é obrigatório.");

        validateHierarchy(requesterId, requesterRole, dto.getTargetUserId());
        validateBlock(dto);

        User user = userRepository.findById(dto.getTargetUserId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        ensureNoBlockOverlap(dto.getTargetUserId(), dto.getStartDate(), dto.getEndDate(),
                dto.isFullDay(), dto.getStartTime(), dto.getEndTime());

        ScheduleBlock block = dto.isFullDay()
                ? ScheduleBlock.createFullDayBlock(user, dto.getStartDate(), dto.getEndDate(), dto.getReason(), BlockType.BLOCKED)
                : ScheduleBlock.createPartialBlock(user, dto.getStartDate(), dto.getStartTime(), dto.getEndTime(), dto.getReason(), BlockType.BLOCKED);

        blockRepo.save(block);
    }

    @Transactional(readOnly = true)
    public List<ScheduleBlock> listBlocks(Long requesterId, String requesterRole, Long targetUserId) {
        validateHierarchy(requesterId, requesterRole, targetUserId);
        return blockRepo.findAllByUser_Id(targetUserId);
    }

    @Transactional
    public void createDefaultSchedule(Long userId) {
        if (userId == null) throw new BadRequestException("Usuário inválido.");

        WorkSchedule existing = scheduleRepo.findByUserId(userId).orElse(null);
        if (existing != null) return;

        WorkSchedule schedule = new WorkSchedule();
        schedule.setUserId(userId);
        schedule.setDays(new ArrayList<>());

        // ✅ Ordem correta: SUNDAY primeiro (para bater com o front)
        List<DayOfWeek> allDays = Arrays.asList(
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        );

        for (DayOfWeek d : allDays) {
            ScheduleDay day = new ScheduleDay();
            day.setWorkSchedule(schedule);
            day.setDayOfWeek(d);

            // Domingo inativo por padrão
            if (d == DayOfWeek.SUNDAY) {
                day.setActive(false);
                day.setStartTime(null);
                day.setEndTime(null);
                day.setBreaks(new ArrayList<>());
            } else {
                day.setActive(true);
                day.setStartTime(LocalTime.of(8, 0));
                day.setEndTime(LocalTime.of(18, 0));
                day.setBreaks(new ArrayList<>());
            }

            schedule.getDays().add(day);
        }

        scheduleRepo.save(schedule);
    }

    private void validateHierarchy(Long reqId, String reqRole, Long targetId) {
        if (reqId == null) throw new BadRequestException("Sessão inválida.");
        if (targetId == null) throw new BadRequestException("Usuário alvo inválido.");

        // ✅ Usuário sempre pode gerenciar a própria agenda
        if (reqId.equals(targetId)) return;

        String role = normalizeRole(reqRole);

        // ✅ DEV pode gerenciar qualquer um
        if ("DEV".equals(role)) return;

        // ✅ STAFF só pode gerenciar a si mesmo
        if ("STAFF".equals(role)) {
            throw new ForbiddenException("Você não tem permissão para gerenciar agenda de outros usuários.");
        }

        // ✅ ADMIN só pode gerenciar STAFF
        if ("ADMIN".equals(role) || "ADM".equals(role)) {
            User target = userRepository.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("Usuário alvo não encontrado."));

            String targetRole = target.getRole().name();
            if (!"STAFF".equalsIgnoreCase(targetRole)) {
                throw new ForbiddenException("ADMIN só pode gerenciar STAFF.");
            }
            return;
        }

        throw new ForbiddenException("Role inválida.");
    }

    private String normalizeRole(String r) {
        return r == null ? "STAFF" : r.trim().toUpperCase();
    }

    private ProfessionalDTO toProfessional(User u) {
        return new ProfessionalDTO(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.isActive());
    }

    private Comparator<ProfessionalDTO> sortActivesFirstByName() {
        return (a, b) -> {
            if (a.isActive() != b.isActive()) return a.isActive() ? -1 : 1;
            String an = a.getName() == null ? "" : a.getName();
            String bn = b.getName() == null ? "" : b.getName();
            return an.compareToIgnoreCase(bn);
        };
    }

    private ScheduleDTOs.DayConfig mapDayToDto(ScheduleDay day) {
        ScheduleDTOs.DayConfig d = new ScheduleDTOs.DayConfig();
        d.setDayOfWeek(day.getDayOfWeek());
        d.setActive(day.isActive());
        d.setStartTime(day.getStartTime());
        d.setEndTime(day.getEndTime());

        List<ScheduleDTOs.IntervalDTO> list = new ArrayList<>();
        if (day.getBreaks() != null) {
            for (TimeInterval b : day.getBreaks()) {
                ScheduleDTOs.IntervalDTO dto = new ScheduleDTOs.IntervalDTO();
                dto.setStart(b.getStart());
                dto.setEnd(b.getEnd());
                list.add(dto);
            }
        }
        d.setBreaks(list);
        return d;
    }

    private ScheduleDay mapDtoToDay(ScheduleDTOs.DayConfig dto, WorkSchedule schedule) {
        ScheduleDay day = new ScheduleDay();
        day.setWorkSchedule(schedule);
        day.setDayOfWeek(dto.getDayOfWeek());
        day.setActive(dto.isActive());

        if (!dto.isActive()) {
            day.setStartTime(null);
            day.setEndTime(null);
            day.setBreaks(new ArrayList<>());
            return day;
        }

        day.setStartTime(dto.getStartTime());
        day.setEndTime(dto.getEndTime());

        List<TimeInterval> breaks = new ArrayList<>();
        if (dto.getBreaks() != null) {
            for (ScheduleDTOs.IntervalDTO b : dto.getBreaks()) {
                breaks.add(new TimeInterval(b.getStart(), b.getEnd()));
            }
        }
        day.setBreaks(breaks);
        return day;
    }

    private void validateDaysPayload(List<ScheduleDTOs.DayConfig> days) {
        if (days == null || days.isEmpty()) throw new BadRequestException("A lista de dias é obrigatória.");
        if (days.size() != 7) throw new BadRequestException("A agenda deve conter exatamente 7 dias.");

        long unique = days.stream()
                .map(ScheduleDTOs.DayConfig::getDayOfWeek)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        if (unique != 7) throw new BadRequestException("A agenda deve conter os 7 dias sem repetição.");
    }

    private void validateTimeLogic(List<ScheduleDTOs.DayConfig> days) {
        for (ScheduleDTOs.DayConfig d : days) {
            if (d.getDayOfWeek() == null) throw new BadRequestException("Dia da semana inválido.");

            if (!d.isActive()) continue;

            if (d.getStartTime() == null || d.getEndTime() == null)
                throw new BadRequestException("Dia ativo deve ter início e fim.");

            if (!d.getStartTime().isBefore(d.getEndTime()))
                throw new BadRequestException("Início deve ser antes do fim.");

            if (d.getBreaks() == null || d.getBreaks().isEmpty()) continue;

            List<ScheduleDTOs.IntervalDTO> sorted = d.getBreaks().stream()
                    .sorted(Comparator.comparing(ScheduleDTOs.IntervalDTO::getStart))
                    .collect(Collectors.toList());

            LocalTime lastEnd = null;

            for (ScheduleDTOs.IntervalDTO b : sorted) {
                if (b.getStart() == null || b.getEnd() == null)
                    throw new BadRequestException("Pausa deve ter início e fim.");

                if (!b.getStart().isBefore(b.getEnd()))
                    throw new BadRequestException("Pausa inválida: início deve ser antes do fim.");

                if (b.getStart().isBefore(d.getStartTime()) || b.getEnd().isAfter(d.getEndTime()))
                    throw new BadRequestException("Pausa deve estar dentro do expediente.");

                if (lastEnd != null && b.getStart().isBefore(lastEnd))
                    throw new BadRequestException("Pausas não podem se sobrepor.");

                lastEnd = b.getEnd();
            }
        }
    }

    private void validateBlock(BlockRequestDTO dto) {
        if (dto.getStartDate() == null || dto.getEndDate() == null)
            throw new BadRequestException("Data inicial e final são obrigatórias.");

        if (dto.getStartDate().isAfter(dto.getEndDate()))
            throw new BadRequestException("Data final deve ser >= data inicial.");

        if (!dto.isFullDay()) {
            if (dto.getStartTime() == null || dto.getEndTime() == null)
                throw new BadRequestException("Bloqueio parcial exige startTime e endTime.");

            if (!dto.getStartTime().isBefore(dto.getEndTime()))
                throw new BadRequestException("startTime deve ser antes de endTime.");

            if (!dto.getStartDate().equals(dto.getEndDate()))
                throw new BadRequestException("Bloqueio parcial deve ser em um único dia.");
        }
    }

    private void ensureNoBlockOverlap(Long userId, LocalDate s, LocalDate e, boolean fullDay, LocalTime st, LocalTime et) {
        List<ScheduleBlock> blocks = blockRepo.findAllByUser_Id(userId);
        if (blocks == null || blocks.isEmpty()) return;

        for (ScheduleBlock b : blocks) {
            boolean dateOverlap = !(e.isBefore(b.getStartDate()) || s.isAfter(b.getEndDate()));
            if (!dateOverlap) continue;

            if (fullDay || b.isFullDay()) {
                throw new ConflictException("Conflito: já existe bloqueio nesse período.");
            }

            if (s.equals(b.getStartDate())) {
                boolean overlap = st.isBefore(b.getEndTime()) && et.isAfter(b.getStartTime());
                if (overlap) throw new ConflictException("Conflito: já existe bloqueio nesse horário.");
            }
        }
    }
}