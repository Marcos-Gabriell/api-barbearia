package br.com.barbearia.apibarbearia.users.entity;

import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import lombok.*;
import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_invites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used; // Garante uso único

    private Instant createdAt;
}