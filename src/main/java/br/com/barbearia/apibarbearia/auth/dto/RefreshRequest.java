package br.com.barbearia.apibarbearia.auth.dto;

import javax.validation.constraints.NotBlank;

public class RefreshRequest {
    @NotBlank(message = "Refresh token é obrigatório.")
    public String refreshToken;
}
