package br.com.barbearia.apibarbearia.auth.dto;

import javax.validation.constraints.NotBlank;

public class LogoutRequest {
    @NotBlank(message = "Token é obrigatório.")
    public String token;
}
