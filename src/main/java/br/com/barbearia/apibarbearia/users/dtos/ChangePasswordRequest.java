package br.com.barbearia.apibarbearia.users.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "Senha atual é obrigatória.")
    public String currentPassword;

    @NotBlank(message = "Nova senha é obrigatória.")
    @Size(min = 5, max = 60, message = "A senha deve ter entre 5 e 60 caracteres.")
    public String newPassword;

    @NotBlank(message = "Confirmação da senha é obrigatória.")
    public String confirmPassword;
}
