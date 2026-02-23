package br.com.barbearia.apibarbearia.appointment.entity.enums;

public enum AppointmentStatus {
    PENDING,    // Agendamento criado, aguardando cliente
    CONFIRMED,  // Barbeiro confirmou que cliente chegou
    CANCELLED,  // Agendamento cancelado
    NO_SHOW     // Cliente não compareceu (auto ou manual)
}
