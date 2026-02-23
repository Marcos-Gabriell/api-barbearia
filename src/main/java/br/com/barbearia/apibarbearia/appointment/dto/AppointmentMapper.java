package br.com.barbearia.apibarbearia.appointment.dto;


import br.com.barbearia.apibarbearia.appointment.dto.response.*;
import br.com.barbearia.apibarbearia.appointment.entity.Appointment;

public class AppointmentMapper {

    public static AppointmentResponse toResponse(Appointment a) {

        return AppointmentResponse.builder()
                .id(a.getId())
                .clientName(a.getClientName())
                .clientEmail(a.getClientEmail())
                .clientPhone(a.getClientPhone())
                .serviceId(a.getServiceId())
                .serviceName(a.getServiceName())
                .durationMinutes(a.getDurationMinutes())
                .professionalUserId(a.getProfessionalUserId())
                .professionalName(a.getProfessionalName())
                .startAt(a.getStartAt())
                .endAt(a.getEndAt())
                .status(a.getStatus())
                .build();
    }

    public static AppointmentDetailResponse toDetailResponse(Appointment a) {

        return AppointmentDetailResponse.builder()
                .id(a.getId())
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
                .cancelReason(a.getCancelReason())
                .cancelMessage(a.getCancelMessage())
                .createdAt(a.getCreatedAt())
                .canceledAt(a.getCanceledAt())
                .confirmedAt(a.getConfirmedAt())
                .build();
    }

}
