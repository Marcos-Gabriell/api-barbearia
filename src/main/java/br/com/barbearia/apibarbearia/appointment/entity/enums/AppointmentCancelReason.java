package br.com.barbearia.apibarbearia.appointment.entity.enums;

/**
 * Motivo/origem do cancelamento do agendamento.
 */
public enum AppointmentCancelReason {

    /** Cancelamento feito por DEV */
    DEV,

    /** Cancelamento feito por ADMIN */
    ADMIN,

    /** Cancelamento feito por STAFF */
    STAFF,

    /** Cancelamento feito pelo CLIENTE via link */
    CLIENT,

    /** No-show automático pelo sistema (scheduler) */
    SYSTEM_NO_SHOW
}