package br.com.barbearia.apibarbearia.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Senha atual é obrigatória.")
    private String currentPassword;

    @NotBlank(message = "Nova senha é obrigatória.")
    @Size(min = 5, max = 60, message = "A senha deve ter entre 5 e 60 caracteres.")
    private String newPassword;

    @NotBlank(message = "Confirmação da senha é obrigatória.")
    private String confirmNewPassword;
}