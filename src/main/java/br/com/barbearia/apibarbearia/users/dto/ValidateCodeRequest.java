package br.com.barbearia.apibarbearia.users.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ValidateCodeRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String code;
}