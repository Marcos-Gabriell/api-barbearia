package br.com.barbearia.apibarbearia.appointment.service;

import br.com.barbearia.apibarbearia.appointment.dto.request.CreateAppointmentInternalRequest;
import br.com.barbearia.apibarbearia.appointment.dto.response.AppointmentCreatedResponse;
import br.com.barbearia.apibarbearia.appointment.dto.response.AppointmentSlotResponse;
import br.com.barbearia.apibarbearia.appointment.dto.response.CancelInfoResponse;
import br.com.barbearia.apibarbearia.appointment.entity.Appointment;
import br.com.barbearia.apibarbearia.appointment.entity.AppointmentCancelToken;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentCancelReason;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import br.com.barbearia.apibarbearia.appointment.events.AppointmentChangedEvent;
import br.com.barbearia.apibarbearia.appointment.events.AppointmentEventType;
import br.com.barbearia.apibarbearia.appointment.repository.AppointmentCancelTokenRepository;
import br.com.barbearia.apibarbearia.appointment.repository.AppointmentRepository;
import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.ConflictException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentPublicService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentCancelTokenRepository tokenRepository;
    private final AppointmentAvailabilityFacade availabilityFacade;
    private final ApplicationEventPublisher publisher;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // ════════════════════════════════════════════════════════════════════════
    //  CRIAÇÃO PÚBLICA
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public AppointmentCreatedResponse createPublic(CreateAppointmentInternalRequest req) {

        validateClient(req.getClientName(), req.getClientEmail(), req.getClientPhone());
        validateStart(req.getStartAt());

        CatalogItem service = availabilityFacade.getServiceOrFail(req.getServiceId());
        User professional    = availabilityFacade.getProfessionalOrFail(req.getProfessionalUserId());

        if (!professional.isActive()) throw new BadRequestException("Profissional inativo.");
        availabilityFacade.validateProfessionalIsResponsible(service, professional.getId());

        Integer duration = service.getDurationMinutes();
        if (duration == null || duration < 5) throw new BadRequestException("Duração do serviço inválida.");

        LocalDateTime startAt = req.getStartAt();
        LocalDateTime endAt   = startAt.plusMinutes(duration);

        availabilityFacade.validateWithinWorkSchedule(null, null, professional.getId(), startAt, endAt);

        List<Appointment> overlaps = appointmentRepository.findOverlapsForUpdate(
                professional.getId(), startAt, endAt,
                Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
        );
        if (!overlaps.isEmpty()) throw new ConflictException("Conflito: este horário já está ocupado.");

        Appointment a = Appointment.builder()
                .clientName(req.getClientName().trim())
                .clientEmail(req.getClientEmail().trim().toLowerCase())
                .clientPhone(req.getClientPhone().trim())
                .serviceId(service.getId())
                .serviceName(service.getName())
                .durationMinutes(duration)
                .professionalUserId(professional.getId())
                .professionalName(professional.getName())
                .professionalEmail(professional.getEmail())
                .startAt(startAt)
                .endAt(endAt)
                .status(AppointmentStatus.PENDING)
                .createdByUserId(null)
                .createdByRole("PUBLIC")
                .build();

        Appointment saved = appointmentRepository.save(a);

        String token = UUID.randomUUID() + "-" + UUID.randomUUID();
        AppointmentCancelToken t = AppointmentCancelToken.builder()
                .appointmentId(saved.getId())
                .token(token)
                .expiresAt(saved.getStartAt().minusMinutes(10))
                .build();
        tokenRepository.save(t);

        String cancelLink = frontendUrl + "/cancelar-agendamento?token=" + token;

        publisher.publishEvent(AppointmentChangedEvent.builder()
                .type(AppointmentEventType.CREATED)
                .appointmentId(saved.getId())
                .clientName(saved.getClientName())
                .clientEmail(saved.getClientEmail())
                .professionalName(saved.getProfessionalName())
                .professionalEmail(saved.getProfessionalEmail())
                .serviceName(saved.getServiceName())
                .startAt(saved.getStartAt())
                .cancelLink(cancelLink)
                .build());

        return AppointmentCreatedResponse.builder()
                .message("Agendamento criado com sucesso")
                .appointmentId(saved.getId())
                .status(saved.getStatus().name())
                .serviceName(saved.getServiceName())
                .professionalName(saved.getProfessionalName())
                .startAt(saved.getStartAt())
                .endAt(saved.getEndAt())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CANCELAMENTO POR TOKEN
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public void cancelByToken(String token) {
        if (token == null || token.trim().isEmpty()) throw new BadRequestException("Token inválido.");

        AppointmentCancelToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token inválido."));

        if (t.isUsed())    throw new BadRequestException("Token já utilizado.");
        if (t.isExpired()) throw new BadRequestException("Token expirado.");

        Appointment a = appointmentRepository.findById(t.getAppointmentId())
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Agendamento já cancelado.");
        }

        if (LocalDateTime.now().isAfter(a.getStartAt().minusMinutes(10))) {
            throw new BadRequestException("Cancelamento permitido apenas até 10 minutos antes do horário.");
        }

        a.setStatus(AppointmentStatus.CANCELLED);
        a.setCanceledAt(LocalDateTime.now());
        a.setCancelReason(AppointmentCancelReason.CLIENT);
        appointmentRepository.save(a);

        t.setUsedAt(LocalDateTime.now());
        tokenRepository.save(t);

        publisher.publishEvent(AppointmentChangedEvent.builder()
                .type(AppointmentEventType.CANCELED)
                .appointmentId(a.getId())
                .clientName(a.getClientName())
                .clientEmail(a.getClientEmail())
                .professionalName(a.getProfessionalName())
                .professionalEmail(a.getProfessionalEmail())
                .serviceName(a.getServiceName())
                .startAt(a.getStartAt())
                .build());
    }

    @Transactional(readOnly = true)
    public CancelInfoResponse getCancelInfo(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Token inválido.");
        }

        AppointmentCancelToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token inválido ou expirado."));

        if (t.isUsed()) {
            throw new BadRequestException("Token já utilizado. O agendamento já foi cancelado.");
        }

        if (t.isExpired()) {
            throw new BadRequestException("Token expirado. Não é mais possível cancelar.");
        }

        Appointment a = appointmentRepository.findById(t.getAppointmentId())
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Agendamento já cancelado.");
        }

        return CancelInfoResponse.builder()
                .clientName(a.getClientName())
                .serviceName(a.getServiceName())
                .professionalName(a.getProfessionalName())
                .startAt(a.getStartAt())
                .code(a.getCode())
                .build();
    }


    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> listSlots(Long serviceId, Long professionalId, LocalDate date) {
        return availabilityFacade.listAvailableSlots(serviceId, professionalId, date, 5);
    }

    private void validateStart(LocalDateTime startAt) {
        if (startAt == null) throw new BadRequestException("startAt é obrigatório.");
        if (startAt.isBefore(LocalDateTime.now())) throw new BadRequestException("Não é possível agendar no passado.");
    }

    private void validateClient(String name, String email, String phone) {
        if (name  == null || name.trim().length() < 3)                          throw new BadRequestException("Nome inválido.");
        if (email == null || email.trim().isEmpty())                            throw new BadRequestException("E-mail é obrigatório.");
        if (phone == null || phone.trim().length() < 11 || phone.trim().length() > 15) throw new BadRequestException("Telefone inválido.");
    }
}