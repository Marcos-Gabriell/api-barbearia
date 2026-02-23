package br.com.barbearia.apibarbearia.appointment.spec;

import br.com.barbearia.apibarbearia.appointment.entity.Appointment;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppointmentSpecifications {

    private AppointmentSpecifications() {}

    public static Specification<Appointment> byCode(String code) {
        return (root, query, cb) -> {
            if (code == null || code.trim().isEmpty()) return null;
            return cb.equal(root.get("code"), code.trim());
        };
    }

    public static Specification<Appointment> status(AppointmentStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Appointment> professionalId(Long professionalId) {
        return (root, query, cb) -> {
            if (professionalId == null) return null;
            return cb.equal(root.get("professionalUserId"), professionalId);
        };
    }

    public static Specification<Appointment> serviceId(Long serviceId) {
        return (root, query, cb) -> {
            if (serviceId == null) return null;
            return cb.equal(root.get("serviceId"), serviceId);
        };
    }

    public static Specification<Appointment> between(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;

            if (from != null && to != null) {
                LocalDateTime fromDT = from.atStartOfDay();
                LocalDateTime toDT = to.plusDays(1).atStartOfDay();
                return cb.and(
                        cb.greaterThanOrEqualTo(root.get("startAt"), fromDT),
                        cb.lessThan(root.get("startAt"), toDT)
                );
            }

            if (from != null) {
                LocalDateTime fromDT = from.atStartOfDay();
                return cb.greaterThanOrEqualTo(root.get("startAt"), fromDT);
            }

            LocalDateTime toDT = to.plusDays(1).atStartOfDay();
            return cb.lessThan(root.get("startAt"), toDT);
        };
    }
    public static Specification<Appointment> clientLike(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) return null;

            String pattern = "%" + searchTerm.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("clientName")), pattern),
                    cb.like(cb.lower(root.get("clientEmail")), pattern),
                    cb.like(root.get("clientPhone"), "%" + searchTerm.trim() + "%"),
                    cb.like(cb.lower(root.get("serviceName")), pattern)
            );
        };
    }

    public static Specification<Appointment> searchTerm(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return null;

            String term = q.trim();

            if (term.matches("\\d{4}-\\d{4}")) {
                return cb.equal(root.get("code"), term);
            }

            String pattern = "%" + term.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("clientName")), pattern),
                    cb.like(cb.lower(root.get("clientEmail")), pattern),
                    cb.like(root.get("clientPhone"), "%" + term + "%"),
                    cb.like(cb.lower(root.get("serviceName")), pattern),
                    cb.like(root.get("code"), "%" + term + "%")
            );
        };
    }
}