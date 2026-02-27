package br.com.barbearia.apibarbearia.appointment.service;

import br.com.barbearia.apibarbearia.appointment.dto.request.*;
import br.com.barbearia.apibarbearia.appointment.dto.response.AppointmentDetailResponse;
import br.com.barbearia.apibarbearia.appointment.entity.*;
import br.com.barbearia.apibarbearia.appointment.entity.enums.*;
import br.com.barbearia.apibarbearia.appointment.events.*;
import br.com.barbearia.apibarbearia.appointment.repository.*;
import br.com.barbearia.apibarbearia.appointment.spec.AppointmentSpecifications;
import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.ConflictException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentCancelTokenRepository tokenRepository;
    private final AppointmentAvailabilityFacade availabilityFacade;
    private final AppointmentAccessService accessService;
    private final AppointmentCodeService codeService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    public Page<AppointmentDetailResponse> list(AppointmentFilterRequest filter, Long requesterId, String requesterRole) {
        Long professionalFilterId = filter != null ? filter.getProfessionalUserId() : null;
        accessService.ensureCanList(requesterRole, requesterId, professionalFilterId);

        int page = filter != null && filter.getPage() != null ? Math.max(filter.getPage(), 0) : 0;
        int size = filter != null && filter.getSize() != null ? Math.min(Math.max(filter.getSize(), 1), 50) : 10;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startAt"));
        Specification<Appointment> spec = Specification.where(null);

        if (filter != null) {
            spec = spec.and(AppointmentSpecifications.status(filter.getStatus()));
            spec = spec.and(AppointmentSpecifications.professionalId(filter.getProfessionalUserId()));
            spec = spec.and(AppointmentSpecifications.serviceId(filter.getServiceId()));
            spec = spec.and(AppointmentSpecifications.between(filter.getFrom(), filter.getTo()));
            String q = firstNonEmpty(filter.getQ(), filter.getClientName(), filter.getClientEmail(), filter.getClientPhone());
            spec = spec.and(AppointmentSpecifications.searchTerm(q));
        }

        Page<Appointment> appointments = appointmentRepository.findAll(spec, pageable);
        return appointments.map(this::toDetailResponse);
    }

    @Transactional(readOnly = true)
    public AppointmentDetailResponse getDetails(Long appointmentId, Long requesterId, String requesterRole) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
        accessService.ensureCanManageAppointment(requesterRole, requesterId, a.getProfessionalUserId());
        return toDetailResponse(a);
    }

    @Transactional(readOnly = true)
    public AppointmentDetailResponse getDetailsByCode(String code, Long requesterId, String requesterRole) {
        if (!codeService.isValidFormat(code)) {
            throw new BadRequestException("Código de agendamento inválido.");
        }
        Appointment a = appointmentRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
        accessService.ensureCanManageAppointment(requesterRole, requesterId, a.getProfessionalUserId());
        return toDetailResponse(a);
    }

    @Transactional
    public AppointmentDetailResponse createInternal(CreateAppointmentInternalRequest req, Long requesterId, String requesterRole) {
        validateClient(req.getClientName(), req.getClientEmail(), req.getClientPhone());
        validateStart(req.getStartAt());

        CatalogItem service = availabilityFacade.getServiceOrFail(req.getServiceId());
        User professional = availabilityFacade.getProfessionalOrFail(req.getProfessionalUserId());

        if (!professional.isActive()) throw new BadRequestException("Profissional inativo.");
        availabilityFacade.validateProfessionalIsResponsible(service, professional.getId());

        int duration = service.getDurationMinutes();
        LocalDateTime startAt = req.getStartAt();
        LocalDateTime endAt = startAt.plusMinutes(duration);

        availabilityFacade.validateWithinWorkSchedule(requesterId, requesterRole, professional.getId(), startAt, endAt);

        List<Appointment> overlaps = appointmentRepository.findOverlapsForUpdate(
                professional.getId(), startAt, endAt,
                Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
        );
        if (!overlaps.isEmpty()) throw new ConflictException("Conflito: este horário já está ocupado.");

        User creator = userRepository.findById(requesterId).orElse(null);
        String creatorUsername = creator != null ? creator.getName() : null;
        String creatorEmail = creator != null ? creator.getEmail() : null;

        String code = codeService.generateCode();
        LocalDateTime now = LocalDateTime.now();

        Appointment a = Appointment.builder()
                .code(code)
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
                .createdAt(now)
                .createdByUserId(requesterId)
                .createdByRole(accessService.normalizeRole(requesterRole))
                .createdByUsername(creatorUsername)
                .createdByEmail(creatorEmail)
                .build();

        Appointment saved = appointmentRepository.save(a);

        String token = generateToken();
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
                .appointmentCode(saved.getCode())
                .clientName(saved.getClientName())
                .clientEmail(saved.getClientEmail())
                .professionalName(saved.getProfessionalName())
                .professionalEmail(saved.getProfessionalEmail())
                .serviceName(saved.getServiceName())
                .startAt(saved.getStartAt())
                .durationMinutes(saved.getDurationMinutes())
                .cancelLink(cancelLink)
                .createdAt(now)
                .createdByUsername(creatorUsername)
                .createdByRole(accessService.normalizeRole(requesterRole))
                .build());

        return toDetailResponse(saved);
    }

    @Transactional
    public void cancelInternal(Long appointmentId, CancelAppointmentInternalRequest req, Long requesterId, String requesterRole) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        accessService.ensureCanManageAppointment(requesterRole, requesterId, a.getProfessionalUserId());
        ensureCancelableByTime(a);

        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BadRequestException("Agendamento já está cancelado ou marcado como no-show.");
        }

        // Busca o usuário que está cancelando para pegar o nome real
        User canceler = userRepository.findById(requesterId).orElse(null);
        String cancelerUsername = canceler != null ? canceler.getName() : "Administração";
        String cancelerEmail = canceler != null ? canceler.getEmail() : null;

        LocalDateTime now = LocalDateTime.now();
        String cancelMsg = normalizeMsg(req != null ? req.getMessage() : null);

        // Atualiza a Entidade
        a.setStatus(AppointmentStatus.CANCELLED);
        a.setCanceledAt(now);
        a.setCancelMessage(cancelMsg);
        a.setCancelReason(mapReason(requesterRole));
        a.setCancelOrigin("INTERNAL");
        a.setCanceledByUserId(requesterId);
        a.setCanceledByRole(accessService.normalizeRole(requesterRole));
        a.setCanceledByUsername(cancelerUsername);
        a.setCanceledByEmail(cancelerEmail);
        a.setUpdatedAt(now);

        appointmentRepository.save(a);

        // Dispara o Evento com todos os campos necessários para o e-mail
        publisher.publishEvent(AppointmentChangedEvent.builder()
                .type(AppointmentEventType.CANCELED)
                .appointmentId(a.getId())
                .appointmentCode(a.getCode())
                .clientName(a.getClientName())
                .clientEmail(a.getClientEmail())
                .professionalName(a.getProfessionalName())
                .professionalEmail(a.getProfessionalEmail())
                .serviceName(a.getServiceName())
                .startAt(a.getStartAt())
                .canceledAt(now)
                .canceledByUsername(cancelerUsername)
                .cancelOrigin("INTERNAL")
                .cancelMessage(cancelMsg) // CAMPO DA MENSAGEM
                .build());
    }

    @Transactional
    public void cancelByToken(String token) {
        AppointmentCancelToken cancelToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token inválido ou expirado."));

        if (LocalDateTime.now().isAfter(cancelToken.getExpiresAt())) {
            throw new BadRequestException("Token expirado. Não é possível cancelar.");
        }

        Appointment a = appointmentRepository.findById(cancelToken.getAppointmentId())
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BadRequestException("Agendamento já está cancelado.");
        }

        LocalDateTime now = LocalDateTime.now();

        a.setStatus(AppointmentStatus.CANCELLED);
        a.setCanceledAt(now);
        a.setCancelReason(AppointmentCancelReason.CLIENT);
        a.setCancelOrigin("CLIENT");
        a.setCanceledByUsername(a.getClientName());
        a.setCanceledByEmail(a.getClientEmail());
        a.setUpdatedAt(now);

        appointmentRepository.save(a);
        tokenRepository.delete(cancelToken);

        publisher.publishEvent(AppointmentChangedEvent.builder()
                .type(AppointmentEventType.CANCELED)
                .appointmentId(a.getId())
                .appointmentCode(a.getCode())
                .clientName(a.getClientName())
                .clientEmail(a.getClientEmail())
                .professionalName(a.getProfessionalName())
                .professionalEmail(a.getProfessionalEmail())
                .serviceName(a.getServiceName())
                .startAt(a.getStartAt())
                .canceledAt(now)
                .canceledByUsername(a.getClientName())
                .cancelOrigin("CLIENT")
                .build());
    }

    @Transactional
    public void confirmArrival(Long appointmentId, Long requesterId, String requesterRole) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        accessService.ensureCanManageAppointment(requesterRole, requesterId, a.getProfessionalUserId());

        if (a.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Só é possível confirmar agendamento com status PENDING.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = a.getStartAt();
        LocalDateTime minTime = startAt.minusMinutes(10);
        LocalDateTime maxTime = startAt.plusMinutes(10);

        if (now.isBefore(minTime)) {
            throw new BadRequestException("Confirmação permitida apenas a partir de 10 minutos antes do horário agendado.");
        }
        if (now.isAfter(maxTime)) {
            throw new BadRequestException("Tempo limite para confirmação expirado.");
        }

        User confirmer = userRepository.findById(requesterId).orElse(null);
        String confirmerUsername = confirmer != null ? confirmer.getName() : null;
        String confirmerEmail = confirmer != null ? confirmer.getEmail() : null;

        a.setStatus(AppointmentStatus.CONFIRMED);
        a.setConfirmedAt(now);
        a.setConfirmedByUserId(requesterId);
        a.setConfirmedByRole(accessService.normalizeRole(requesterRole));
        a.setConfirmedByUsername(confirmerUsername);
        a.setConfirmedByEmail(confirmerEmail);
        a.setUpdatedAt(now);

        appointmentRepository.save(a);

        publisher.publishEvent(AppointmentChangedEvent.builder()
                .type(AppointmentEventType.CONFIRMED)
                .appointmentId(a.getId())
                .appointmentCode(a.getCode())
                .clientName(a.getClientName())
                .clientEmail(a.getClientEmail())
                .professionalName(a.getProfessionalName())
                .professionalEmail(a.getProfessionalEmail())
                .serviceName(a.getServiceName())
                .startAt(a.getStartAt())
                .confirmedAt(now)
                .confirmedByUsername(confirmerUsername)
                .confirmedByRole(accessService.normalizeRole(requesterRole))
                .build());
    }

    @Transactional
    public void markNoShow(Long appointmentId, Long requesterId, String requesterRole) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        accessService.ensureCanManageAppointment(requesterRole, requesterId, a.getProfessionalUserId());

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(a.getStartAt())) {
            throw new BadRequestException("Só é possível marcar no-show após o horário do agendamento.");
        }
        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BadRequestException("Agendamento já está cancelado ou marcado como no-show.");
        }
        if (a.getStatus() == AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Agendamento já foi confirmado. Não é possível marcar como no-show.");
        }

        User marker = userRepository.findById(requesterId).orElse(null);
        String markerUsername = marker != null ? marker.getName() : null;

        a.setStatus(AppointmentStatus.NO_SHOW);
        a.setNoShowAt(now);
        a.setNoShowByUserId(requesterId);
        a.setNoShowByRole(accessService.normalizeRole(requesterRole));
        a.setNoShowByUsername(markerUsername);
        a.setCancelReason(AppointmentCancelReason.STAFF);
        a.setUpdatedAt(now);

        appointmentRepository.save(a);
    }

    private AppointmentDetailResponse toDetailResponse(Appointment a) {
        return AppointmentDetailResponse.builder()
                .id(a.getId())
                .code(a.getCode())
                .clientName(a.getClientName())
                .clientEmail(a.getClientEmail())
                .clientPhone(a.getClientPhone())
                .serviceId(a.getServiceId())
                .serviceName(a.getServiceName())
                .durationMinutes(a.getDurationMinutes())
                .professionalUserId(a.getProfessionalUserId())
                .professionalName(a.getProfessionalName())
                .professionalEmail(a.getProfessionalEmail())
                .startAt(a.getStartAt())
                .endAt(a.getEndAt())
                .status(a.getStatus())
                // Criação
                .createdAt(a.getCreatedAt())
                .createdByUserId(a.getCreatedByUserId())
                .createdByRole(a.getCreatedByRole())
                .createdByUsername(a.getCreatedByUsername())
                .createdByEmail(a.getCreatedByEmail())
                .createdByDescription(a.getCreatedByDescription())
                // Confirmação
                .confirmedAt(a.getConfirmedAt())
                .confirmedByUserId(a.getConfirmedByUserId())
                .confirmedByRole(a.getConfirmedByRole())
                .confirmedByUsername(a.getConfirmedByUsername())
                .confirmedByEmail(a.getConfirmedByEmail())
                .confirmedByDescription(a.getConfirmedByDescription())
                // Cancelamento
                .canceledAt(a.getCanceledAt())
                .cancelReason(a.getCancelReason())
                .cancelMessage(a.getCancelMessage())
                .canceledByUserId(a.getCanceledByUserId())
                .canceledByRole(a.getCanceledByRole())
                .canceledByUsername(a.getCanceledByUsername())
                .canceledByEmail(a.getCanceledByEmail())
                .cancelOrigin(a.getCancelOrigin())
                .canceledByDescription(a.getCanceledByDescription())
                // No-show
                .noShowAt(a.getNoShowAt())
                .noShowByUserId(a.getNoShowByUserId())
                .noShowByRole(a.getNoShowByRole())
                .noShowByUsername(a.getNoShowByUsername())
                .noShowByDescription(a.getNoShowByDescription())
                // Outros
                .updatedAt(a.getUpdatedAt())
                .canCancel(a.isCancelable())
                .canConfirm(a.isConfirmable())
                .canMarkNoShow(a.isNoShowMarkable())
                .build();
    }

    private void validateStart(LocalDateTime startAt) {
        if (startAt == null) throw new BadRequestException("startAt é obrigatório.");
        if (startAt.isBefore(LocalDateTime.now())) throw new BadRequestException("Não é possível agendar no passado.");
    }

    private void validateClient(String name, String email, String phone) {
        if (name == null || name.trim().length() < 3) throw new BadRequestException("Nome inválido (mínimo 3 caracteres).");
        if (email == null || email.trim().isEmpty()) throw new BadRequestException("E-mail é obrigatório.");
        if (phone == null || phone.trim().length() < 11 || phone.trim().length() > 15)
            throw new BadRequestException("Telefone inválido (11 a 15 dígitos).");
    }

    private void ensureCancelableByTime(Appointment a) {
        if (LocalDateTime.now().isAfter(a.getStartAt().minusMinutes(10))) {
            throw new BadRequestException("Cancelamento permitido apenas até 10 minutos antes do horário agendado.");
        }
    }

    private AppointmentCancelReason mapReason(String role) {
        String r = accessService.normalizeRole(role);
        if ("DEV".equals(r)) return AppointmentCancelReason.DEV;
        if ("ADMIN".equals(r) || "ADM".equals(r)) return AppointmentCancelReason.ADMIN;
        return AppointmentCancelReason.STAFF;
    }

    private String normalizeMsg(String m) {
        if (m == null) return null;
        String s = m.trim();
        if (s.isEmpty()) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    private String generateToken() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

    private String firstNonEmpty(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) return v;
        }
        return null;
    }
}