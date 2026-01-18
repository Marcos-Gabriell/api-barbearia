package br.com.barbearia.apibarbearia.users.dtos;

import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class UserResponse {

    public Long id;
    public String name;
    public String email;
    public Role role;
    public boolean active;
    public boolean mustChangePassword;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    public Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    public Instant updatedAt;
}
