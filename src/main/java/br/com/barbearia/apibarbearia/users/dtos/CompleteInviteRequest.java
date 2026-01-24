package br.com.barbearia.apibarbearia.users.dtos;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CompleteInviteRequest {
    @NotBlank(message = "O token é obrigatório")
    private String token;

    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @NotBlank(message = "O telefone é obrigatório")
    private String phone;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "A confirmação de senha é obrigatória")
    private String confirmPassword;


}