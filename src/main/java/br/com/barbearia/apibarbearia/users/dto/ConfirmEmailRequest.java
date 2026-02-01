package br.com.barbearia.apibarbearia.users.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmEmailRequest {
    @NotBlank
    private String code;
}