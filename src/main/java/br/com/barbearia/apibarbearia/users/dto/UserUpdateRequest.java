package br.com.barbearia.apibarbearia.users.dto;

import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

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

    private String phone;
}
