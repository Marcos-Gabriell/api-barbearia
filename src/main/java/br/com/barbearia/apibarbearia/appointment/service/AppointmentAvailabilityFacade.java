package br.com.barbearia.apibarbearia.appointment.service;

import br.com.barbearia.apibarbearia.appointment.dto.response.AppointmentSlotResponse;
import br.com.barbearia.apibarbearia.appointment.entity.Appointment;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import br.com.barbearia.apibarbearia.appointment.repository.AppointmentRepository;
import br.com.barbearia.apibarbearia.availability.dto.ScheduleDTOs;
import br.com.barbearia.apibarbearia.availability.entity.ScheduleBlock;
import br.com.barbearia.apibarbearia.availability.service.AvailabilityService;
import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import br.com.barbearia.apibarbearia.catalog.repository.CatalogRepository;
import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AppointmentAvailabilityFacade {

    private final CatalogRepository catalogRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;
    private final AppointmentRepository appointmentRepository;

    public AppointmentAvailabilityFacade(
            CatalogRepository catalogRepository,
            UserRepository userRepository,
            AvailabilityService availabilityService,
            AppointmentRepository appointmentRepository
    ) {
        this.catalogRepository = catalogRepository;
        this.userRepository = userRepository;
        this.availabilityService = availabilityService;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public CatalogItem getServiceOrFail(Long serviceId) {
        return catalogRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado."));
    }

    @Transactional(readOnly = true)
    public User getProfessionalOrFail(Long professionalId) {
        return userRepository.findById(professionalId)
                .orElseThrow(() -> new NotFoundException("Profissional não encontrado."));
    }

    @Transactional(readOnly = true)
    public void validateProfessionalIsResponsible(CatalogItem service, Long professionalId) {
        boolean ok = service.getResponsibles() != null &&
                service.getResponsibles().stream().anyMatch(u -> Objects.equals(u.getId(), professionalId));
        if (!ok) throw new BadRequestException("Profissional não está vinculado a este serviço.");
    }

    // ==========================
    // ✅ VALIDA SE start/end cabem no expediente (com pausas e blocks)
    // ==========================
    @Transactional(readOnly = true)
    public void validateWithinWorkSchedule(Long requesterId, String requesterRole, Long professionalId,
                                           LocalDateTime startAt, LocalDateTime endAt) {

        if (startAt == null || endAt == null) throw new BadRequestException("Horário inválido.");
        if (!startAt.isBefore(endAt)) throw new BadRequestException("startAt deve ser antes de endAt.");

        LocalDate date = startAt.toLocalDate();
        if (!date.equals(endAt.toLocalDate())) {
            throw new BadRequestException("Agendamento deve começar e terminar no mesmo dia.");
        }

        ScheduleDTOs.ScheduleRequest schedule = availabilityService.getSchedule(
                professionalId, // requesterId "fake" por causa do validateHierarchy
                "DEV",
                professionalId
        );

        ScheduleDTOs.DayConfig dayCfg = findDayConfig(schedule, date.getDayOfWeek());
        if (dayCfg == null || !dayCfg.isActive()) throw new BadRequestException("Profissional não atende nesse dia.");

        LocalTime dayStart = dayCfg.getStartTime();
        LocalTime dayEnd = dayCfg.getEndTime();

        LocalTime st = startAt.toLocalTime();
        LocalTime en = endAt.toLocalTime();

        if (dayStart == null || dayEnd == null) throw new BadRequestException("Agenda inválida para o dia.");
        if (st.isBefore(dayStart) || en.isAfter(dayEnd)) throw new BadRequestException("Horário fora do expediente.");

        // expediente menos pausas
        List<Interval> baseIntervals = subtractBreaks(dayStart, dayEnd, dayCfg.getBreaks());

        // aplica blocks
        List<ScheduleBlock> blocks = availabilityService.listBlocks(professionalId, "DEV", professionalId);
        final List<Interval> freeIntervals = applyBlocks(date, baseIntervals, blocks);

        boolean fits = freeIntervals.stream().anyMatch(i -> !st.isBefore(i.getStart()) && !en.isAfter(i.getEnd()));
        if (!fits) throw new BadRequestException("Horário indisponível (pausa/bloqueio).");
    }

    // ==========================
    // ✅ SLOTS DISPONÍVEIS para o público / front
    // ==========================
    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> listAvailableSlots(Long serviceId, Long professionalId,
                                                            LocalDate date, int stepMinutes) {

        if (date == null) throw new BadRequestException("date é obrigatório.");
        if (stepMinutes <= 0) stepMinutes = 5;

        CatalogItem service = getServiceOrFail(serviceId);
        User professional = getProfessionalOrFail(professionalId);

        if (!professional.isActive()) throw new BadRequestException("Profissional inativo.");
        validateProfessionalIsResponsible(service, professionalId);

        Integer duration = service.getDurationMinutes();
        if (duration == null || duration < 5) throw new BadRequestException("Duração do serviço inválida.");

        ScheduleDTOs.ScheduleRequest schedule = availabilityService.getSchedule(professionalId, "DEV", professionalId);
        ScheduleDTOs.DayConfig dayCfg = findDayConfig(schedule, date.getDayOfWeek());

        if (dayCfg == null || !dayCfg.isActive()) return Collections.emptyList();

        LocalTime dayStart = dayCfg.getStartTime();
        LocalTime dayEnd = dayCfg.getEndTime();
        if (dayStart == null || dayEnd == null) return Collections.emptyList();

        // expediente menos pausas
        List<Interval> baseIntervals = subtractBreaks(dayStart, dayEnd, dayCfg.getBreaks());

        // aplica blocks
        List<ScheduleBlock> blocks = availabilityService.listBlocks(professionalId, "DEV", professionalId);
        final List<Interval> intervals = applyBlocks(date, baseIntervals, blocks);

        // agendamentos do dia (ocupados)
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<Appointment> dayAppointments = appointmentRepository.listDayAppointments(
                professionalId,
                from,
                to,
                Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
        );

        final List<IntervalDT> busy = dayAppointments.stream()
                .map(a -> new IntervalDT(a.getStartAt().toLocalTime(), a.getEndAt().toLocalTime()))
                .collect(Collectors.toList());

        List<AppointmentSlotResponse> result = new ArrayList<>();

        for (Interval free : intervals) {
            LocalTime cursor = free.getStart();

            while (!cursor.plusMinutes(duration).isAfter(free.getEnd())) {
                LocalTime slotEnd = cursor.plusMinutes(duration);

                boolean collides = false;
                for (IntervalDT b : busy) {
                    if (overlaps(cursor, slotEnd, b.getStart(), b.getEnd())) {
                        collides = true;
                        break;
                    }
                }


                if (!collides) {
                    result.add(AppointmentSlotResponse.builder()
                            .date(date)
                            .start(cursor)
                            .end(slotEnd)
                            .build());
                }

                cursor = cursor.plusMinutes(stepMinutes);
            }
        }

        if (date.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            return result.stream()
                    .filter(s -> s.getStart().isAfter(now))
                    .collect(Collectors.toList());
        }

        return result;
    }

    private ScheduleDTOs.DayConfig findDayConfig(ScheduleDTOs.ScheduleRequest schedule, DayOfWeek dow) {
        if (schedule == null || schedule.getDays() == null) return null;
        return schedule.getDays().stream()
                .filter(d -> d.getDayOfWeek() == dow)
                .findFirst()
                .orElse(null);
    }

    @Getter
    @AllArgsConstructor
    private static class Interval {
        private LocalTime start;
        private LocalTime end;
    }

    @Getter
    @AllArgsConstructor
    private static class IntervalDT {
        private LocalTime start;
        private LocalTime end;
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    private List<Interval> subtractBreaks(LocalTime dayStart, LocalTime dayEnd, List<ScheduleDTOs.IntervalDTO> breaks) {
        List<Interval> base = new ArrayList<>();
        base.add(new Interval(dayStart, dayEnd));

        if (breaks == null || breaks.isEmpty()) return base;

        List<ScheduleDTOs.IntervalDTO> sorted = breaks.stream()
                .sorted(Comparator.comparing(ScheduleDTOs.IntervalDTO::getStart))
                .collect(Collectors.toList());

        for (ScheduleDTOs.IntervalDTO br : sorted) {
            if (br.getStart() == null || br.getEnd() == null) continue;
            base = subtractOne(base, br.getStart(), br.getEnd());
        }

        return base;
    }

    private List<Interval> subtractOne(List<Interval> intervals, LocalTime subStart, LocalTime subEnd) {
        List<Interval> res = new ArrayList<>();

        for (Interval i : intervals) {
            if (!overlaps(i.getStart(), i.getEnd(), subStart, subEnd)) {
                res.add(i);
                continue;
            }
            if (i.getStart().isBefore(subStart)) {
                res.add(new Interval(i.getStart(), subStart));
            }
            if (i.getEnd().isAfter(subEnd)) {
                res.add(new Interval(subEnd, i.getEnd()));
            }
        }

        return res.stream()
                .filter(x -> x.getStart().isBefore(x.getEnd()))
                .collect(Collectors.toList());
    }

    private List<Interval> applyBlocks(LocalDate date, List<Interval> freeIntervals, List<ScheduleBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) return freeIntervals;

        List<Interval> res = new ArrayList<>(freeIntervals);

        for (ScheduleBlock b : blocks) {
            boolean dateOverlap = !(date.isBefore(b.getStartDate()) || date.isAfter(b.getEndDate()));
            if (!dateOverlap) continue;

            if (b.isFullDay()) return Collections.emptyList();

            if (b.getStartDate() != null && b.getStartDate().equals(date)
                    && b.getStartTime() != null && b.getEndTime() != null) {
                res = subtractOne(res, b.getStartTime(), b.getEndTime());
            }
        }

        return res;
    }
}
