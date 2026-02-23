package br.com.barbearia.apibarbearia.appointment.service;

import br.com.barbearia.apibarbearia.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço para geração de códigos únicos de agendamento.
 *
 * Formato: AAMMXXXX
 * - AA: Ano (2 dígitos)
 * - MM: Mês (2 dígitos)
 * - XXXX: Sequencial do mês (4 dígitos, começa em 0001)
 *
 * Exemplo: 25020001 (primeiro agendamento de fevereiro de 2025)
 */
@Service
@RequiredArgsConstructor
public class AppointmentCodeService {

    private final AppointmentRepository appointmentRepository;

    private static final DateTimeFormatter PREFIX_FORMAT =
            DateTimeFormatter.ofPattern("yyMM");

    /**
     * Gera um novo código único para o agendamento.
     * Thread-safe: usa transação para evitar duplicatas.
     */
    @Transactional
    public String generateCode() {

        String prefix = LocalDate.now().format(PREFIX_FORMAT); // Ex: "2502"

        // Agora buscamos sem traço
        List<String> existingCodes = appointmentRepository.findCodesByPrefix(prefix);

        int nextSequence = 1;

        if (!existingCodes.isEmpty()) {
            String lastCode = existingCodes.get(0);

            // 8 caracteres: 4 prefixo + 4 sequência
            if (lastCode != null && lastCode.length() == 8) {
                try {
                    String sequencePart = lastCode.substring(4); // pega "0001" de "25020001"
                    nextSequence = Integer.parseInt(sequencePart) + 1;
                } catch (NumberFormatException ignored) {
                    nextSequence = 1;
                }
            }
        }

        // Formato final: 25020001
        return prefix + String.format("%04d", nextSequence);
    }

    /**
     * Valida se um código tem o formato correto (AAMMXXXX).
     */
    public boolean isValidFormat(String code) {
        if (code == null || code.length() != 8) return false;
        return code.matches("\\d{8}");
    }
}
