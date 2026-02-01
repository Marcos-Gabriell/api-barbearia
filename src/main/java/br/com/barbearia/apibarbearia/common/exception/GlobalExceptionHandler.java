package br.com.barbearia.apibarbearia.common.exception;

import br.com.barbearia.apibarbearia.availability.common.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(
            ForbiddenException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req.getRequestURI(), null);
    }

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
                "Dados inválidos. Verifique os campos.",
                req.getRequestURI(),
                fieldErrors
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {

        String message = String.format(
                "Método '%s' não suportado para esta rota. Tente: %s",
                ex.getMethod(), ex.getSupportedHttpMethods()
        );

        return build(HttpStatus.METHOD_NOT_ALLOWED, message, req.getRequestURI(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonError(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Formato de requisição inválido (JSON malformado ou tipo de dado incorreto).",
                req.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {

        return build(
                HttpStatus.FORBIDDEN,
                "Acesso negado. Você não tem permissão para realizar esta ação.",
                req.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest req) {

        log.error("ERRO INESPERADO NA API", ex);

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor.",
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
        body.put("timestamp", Instant.now());
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
