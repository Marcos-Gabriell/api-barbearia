package br.com.barbearia.apibarbearia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            BadRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            ConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), null);
    }

    // Erros do @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors
                        .computeIfAbsent(err.getField(), k -> new ArrayList<>())
                        .add(err.getDefaultMessage())
        );

        return build(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos.",
                req.getRequestURI(),
                fieldErrors
        );
    }

    // Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest req) {

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado.",
                req.getRequestURI(),
                null
        );
    }

    private ResponseEntity<Map<String, Object>> build(
            HttpStatus status,
            String message,
            String path,
            Map<String, ?> errors
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);

        if (errors != null && !errors.isEmpty()) {
            body.put("errors", errors);
        }

        return ResponseEntity.status(status).body(body);
    }
}
