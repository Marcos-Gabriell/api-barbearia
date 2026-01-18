package br.com.barbearia.apibarbearia.users.dtos;

import br.com.barbearia.apibarbearia.users.entity.Role.Role;

import javax.validation.constraints.*;

public class UserCreateRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 2, max = 80, message = "Nome deve ter entre 2 e 80 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ ]+$", message = "Nome deve conter apenas letras e espaços.")
    public String name;

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 120, message = "E-mail deve ter no máximo 120 caracteres.")
    public String email;

    @NotNull(message = "Perfil é obrigatório.")
    public Role role;
}
