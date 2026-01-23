package br.com.barbearia.apibarbearia.schedule.service;

import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.schedule.dtos.*;
import br.com.barbearia.apibarbearia.schedule.entity.*;
import br.com.barbearia.apibarbearia.schedule.entity.enums.BlockType;
import br.com.barbearia.apibarbearia.schedule.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    private final WorkingHoursRepository workingHoursRepository;
    private final BreakIntervalRepository breakRepository;
    private final DayOverrideRepository overrideRepository;
    private final TimeBlockRepository blockRepository;

    public ScheduleService(WorkingHoursRepository workingHoursRepository,
                           BreakIntervalRepository breakRepository,
                           DayOverrideRepository overrideRepository,
                           TimeBlockRepository blockRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.breakRepository = breakRepository;
        this.overrideRepository = overrideRepository;
        this.blockRepository = blockRepository;
    }

    // -----------------------------------------------------------------------
    // WORKING HOURS
    // -----------------------------------------------------------------------

    @Transactional
    public WorkingHours upsertWorkingHours(Long professionalId, WorkingHoursUpsertDTO dto) {
        if (dto == null) throw new BadRequestException("Dados inválidos.");
        if (dto.dayOfWeek == null) throw new BadRequestException("dayOfWeek é obrigatório.");
        validateTimeRange(dto.startTime, dto.endTime, "Horário de funcionamento inválido.");

        WorkingHours wh = workingHoursRepository
                .findByProfessionalIdAndDayOfWeek(professionalId, dto.dayOfWeek)
                .orElseGet(WorkingHours::new);

        wh.setProfessionalId(professionalId);
        wh.setDayOfWeek(dto.dayOfWeek);
        wh.setStartTime(dto.startTime);
        wh.setEndTime(dto.endTime);
        wh.setActive(dto.active != null ? dto.active : true);

        return workingHoursRepository.save(wh);
    }

    public List<WorkingHours> listWorkingHours(Long professionalId) {
        return workingHoursRepository.findByProfessionalId(professionalId);
    }

    // -----------------------------------------------------------------------
    // BREAKS
    // -----------------------------------------------------------------------

    @Transactional
    public BreakInterval createBreak(Long professionalId, BreakCreateDTO dto) {
        if (dto == null) throw new BadRequestException("Dados inválidos.");
        if (dto.dayOfWeek == null) throw new BadRequestException("dayOfWeek é obrigatório.");
        validateTimeRange(dto.startTime, dto.endTime, "Pausa inválida.");

        WorkingHours wh = workingHoursRepository
                .findByProfessionalIdAndDayOfWeek(professionalId, dto.dayOfWeek)
                .orElseThrow(() -> new BadRequestException("Defina o horário de funcionamento desse dia antes da pausa."));

        if (!Boolean.TRUE.equals(wh.getActive())) {
            throw new BadRequestException("Dia desativado. Ative o dia antes de criar pausa.");
        }

        // pausa dentro do expediente
        if (dto.startTime.isBefore(wh.getStartTime()) || dto.endTime.isAfter(wh.getEndTime())) {
            throw new BadRequestException("A pausa precisa estar dentro do horário de funcionamento.");
        }

        // sem sobreposição
        List<BreakInterval> existing = breakRepository.findByProfessionalIdAndDayOfWeek(professionalId, dto.dayOfWeek);
        for (BreakInterval b : existing) {
            if (overlaps(dto.startTime, dto.endTime, b.getStartTime(), b.getEndTime())) {
                throw new BadRequestException("Essa pausa colide com outra pausa existente.");
            }
        }

        BreakInterval bi = new BreakInterval();
        bi.setProfessionalId(professionalId);
        bi.setDayOfWeek(dto.dayOfWeek);
        bi.setStartTime(dto.startTime);
        bi.setEndTime(dto.endTime);

        return breakRepository.save(bi);
    }

    public List<BreakInterval> listBreaks(Long professionalId) {
        return breakRepository.findByProfessionalId(professionalId);
    }

    public Optional<BreakInterval> findBreakByIdAndProfessional(Long breakId, Long professionalId) {
        return breakRepository.findById(breakId)
                .filter(b -> b.getProfessionalId().equals(professionalId));
    }

    @Transactional
    public void deleteBreak(Long professionalId, Long breakId) {
        BreakInterval b = breakRepository.findById(breakId)
                .orElseThrow(() -> new NotFoundException("Pausa não encontrada."));

        if (!b.getProfessionalId().equals(professionalId)) {
            throw new BadRequestException("Pausa não pertence a esse profissional.");
        }

        breakRepository.delete(b);
    }

    // -----------------------------------------------------------------------
    // OVERRIDES (exceção por data)
    // -----------------------------------------------------------------------

    @Transactional
    public DayOverride upsertOverride(Long professionalId, DayOverrideUpsertDTO dto) {
        if (dto == null) throw new BadRequestException("Dados inválidos.");
        if (dto.date == null) throw new BadRequestException("date é obrigatório.");

        boolean closed = dto.closed != null && dto.closed;

        if (!closed) {
            if (dto.startTime == null || dto.endTime == null) {
                throw new BadRequestException("startTime e endTime são obrigatórios quando closed=false.");
            }
            validateTimeRange(dto.startTime, dto.endTime, "Override inválido (horário).");
        }

        DayOverride ov = overrideRepository
                .findByProfessionalIdAndDate(professionalId, dto.date)
                .orElseGet(DayOverride::new);

        ov.setProfessionalId(professionalId);
        ov.setDate(dto.date);
        ov.setClosed(closed);
        ov.setStartTime(closed ? null : dto.startTime);
        ov.setEndTime(closed ? null : dto.endTime);
        ov.setNote(dto.note);

        return overrideRepository.save(ov);
    }

    public Optional<DayOverride> findOverrideByIdAndProfessional(Long overrideId, Long professionalId) {
        return overrideRepository.findById(overrideId)
                .filter(o -> o.getProfessionalId().equals(professionalId));
    }

    @Transactional
    public void deleteOverride(Long professionalId, Long overrideId) {
        DayOverride ov = overrideRepository.findById(overrideId)
                .orElseThrow(() -> new NotFoundException("Override não encontrado."));

        if (!ov.getProfessionalId().equals(professionalId)) {
            throw new BadRequestException("Override não pertence a esse profissional.");
        }

        overrideRepository.delete(ov);
    }

    public List<DayOverride> listOverrides(Long professionalId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("from e to são obrigatórios.");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("from não pode ser maior que to.");
        }
        return overrideRepository.findByProfessionalIdAndDateBetween(professionalId, from, to);
    }

    // -----------------------------------------------------------------------
    // BLOCKS (bloqueios por data/faixa)
    // -----------------------------------------------------------------------

    @Transactional
    public TimeBlock createBlock(Long professionalId, BlockCreateDTO dto) {
        if (dto == null) throw new BadRequestException("Dados inválidos.");
        if (dto.type == null) throw new BadRequestException("type é obrigatório.");

        TimeBlock b = new TimeBlock();
        b.setProfessionalId(professionalId);
        b.setType(dto.type);
        b.setReason(dto.reason);

        if (dto.type == BlockType.FULL_DAY) {
            if (dto.date == null) throw new BadRequestException("date é obrigatório para FULL_DAY.");
            b.setDate(dto.date);
            b.setStartAt(null);
            b.setEndAt(null);
        } else {
            if (dto.startAt == null || dto.endAt == null) {
                throw new BadRequestException("startAt e endAt são obrigatórios para RANGE.");
            }
            if (!dto.startAt.isBefore(dto.endAt)) {
                throw new BadRequestException("Bloqueio RANGE inválido (startAt >= endAt).");
            }
            b.setStartAt(dto.startAt);
            b.setEndAt(dto.endAt);

            // opcional: facilita busca por dia
            b.setDate(dto.startAt.toLocalDate());
        }

        return blockRepository.save(b);
    }

    @Transactional
    public void deleteBlock(Long professionalId, Long blockId) {
        TimeBlock b = blockRepository.findById(blockId)
                .orElseThrow(() -> new NotFoundException("Bloqueio não encontrado."));

        if (!b.getProfessionalId().equals(professionalId)) {
            throw new BadRequestException("Bloqueio não pertence a esse profissional.");
        }

        blockRepository.delete(b);
    }

    public List<TimeBlock> listBlocksByDate(Long professionalId, LocalDate date) {
        if (date == null) throw new BadRequestException("date é obrigatório.");
        return blockRepository.findByProfessionalIdAndDate(professionalId, date);
    }

    // -----------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------

    private void validateTimeRange(LocalTime start, LocalTime end, String message) {
        if (start == null || end == null) throw new BadRequestException(message);
        if (!start.isBefore(end)) throw new BadRequestException(message);
    }

    private boolean overlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // overlap se start1 < end2 e start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
