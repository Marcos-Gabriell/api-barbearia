package br.com.barbearia.apibarbearia.appointment.service;

import br.com.barbearia.apibarbearia.availability.common.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class AppointmentAccessService {

    public String normalizeRole(String role) {
        return role == null ? "STAFF" : role.trim().toUpperCase();
    }

    public void ensureCanManageAppointment(String requesterRole, Long requesterId, Long appointmentProfessionalId) {
        String r = normalizeRole(requesterRole);

        if ("DEV".equals(r)) return;
        if ("ADMIN".equals(r) || "ADM".equals(r)) return;

        if ("STAFF".equals(r)) {
            if (requesterId == null || appointmentProfessionalId == null || !requesterId.equals(appointmentProfessionalId)) {
                throw new ForbiddenException("Você só pode gerenciar seus próprios agendamentos.");
            }
            return;
        }

        throw new ForbiddenException("Role inválida.");
    }

    public void ensureCanList(String requesterRole, Long requesterId, Long professionalFilterId) {
        String r = normalizeRole(requesterRole);

        if ("DEV".equals(r)) return;
        if ("ADMIN".equals(r) || "ADM".equals(r)) return;

        if ("STAFF".equals(r)) {
            if (professionalFilterId == null || requesterId == null || !requesterId.equals(professionalFilterId)) {
                throw new ForbiddenException("STAFF deve filtrar pelos próprios agendamentos.");
            }
            return;
        }

        throw new ForbiddenException("Role inválida.");
    }
}
