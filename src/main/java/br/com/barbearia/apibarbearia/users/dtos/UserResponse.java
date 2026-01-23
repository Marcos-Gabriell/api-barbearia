package br.com.barbearia.apibarbearia.users.dtos;

import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;

    private String phone;

    private String pendingEmail;

    private Role role;
    private boolean active;
    private boolean mustChangePassword;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    private Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    private Instant updatedAt;
}