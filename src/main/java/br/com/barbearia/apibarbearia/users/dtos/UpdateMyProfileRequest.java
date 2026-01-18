package br.com.barbearia.apibarbearia.users.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class UpdateMyProfileRequest {

    @NotBlank(message = "Nome é obrigatório")
    public String name;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    public String email;
}
