package br.com.barbearia.apibarbearia.users.dtos;

import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class InviteUserRequest {
    @NotNull(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotNull(message = "O cargo (Role) é obrigatório")
    private Role role;
}