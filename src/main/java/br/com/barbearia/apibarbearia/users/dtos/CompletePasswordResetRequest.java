package br.com.barbearia.apibarbearia.users.dtos;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CompletePasswordResetRequest {
    @NotBlank(message = "O e-mail é obrigatório.")
    private String email;

    @NotBlank(message = "O código é obrigatório.")
    private String code;

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    private String newPassword;

    @NotBlank(message = "A confirmação da senha é obrigatória.")
    private String confirmNewPassword;
}