package br.com.barbearia.apibarbearia.appointment.exception;

public class AppointmentExceptions {

    public static class AppointmentNotFoundException extends RuntimeException {
        public AppointmentNotFoundException(Long id) {
            super("Agendamento #" + id + " não encontrado.");
        }
    }

    public static class TimeSlotNotAvailableException extends RuntimeException {
        public TimeSlotNotAvailableException(String message) {
            super(message);
        }
    }

    public static class InvalidCancellationTokenException extends RuntimeException {
        public InvalidCancellationTokenException() {
            super("Link de cancelamento inválido ou expirado.");
        }
    }

    public static class CancellationDeadlineExceededException extends RuntimeException {
        public CancellationDeadlineExceededException() {
            super("Cancelamento não permitido. O prazo mínimo é de 10 minutos antes do horário agendado.");
        }
    }

    public static class AppointmentAlreadyCancelledException extends RuntimeException {
        public AppointmentAlreadyCancelledException() {
            super("Este agendamento já foi cancelado.");
        }
    }

    public static class InvalidAppointmentStatusException extends RuntimeException {
        public InvalidAppointmentStatusException(String message) {
            super(message);
        }
    }
}