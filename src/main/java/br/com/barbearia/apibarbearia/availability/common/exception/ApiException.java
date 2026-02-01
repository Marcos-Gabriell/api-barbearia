package br.com.barbearia.apibarbearia.availability.common.exception;

public abstract class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
