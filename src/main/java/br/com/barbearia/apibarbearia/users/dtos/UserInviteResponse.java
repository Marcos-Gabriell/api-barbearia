package br.com.barbearia.apibarbearia.users.dtos;

import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserInviteResponse {
    private Long id;
    private String email;
    private Role role;
    private Instant createdAt;
    private Instant expiresAt;
    private String status;
    private String inviteLink;
}