package br.com.barbearia.apibarbearia.users.entity;

import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;


    @Column(name = "phone", length = 15)
    @Pattern(regexp = "^[0-9]*$", message = "O telefone deve conter apenas números")
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "pending_email")
    private String pendingEmail;

    @Column(name = "email_verification_code")
    private String emailVerificationCode;

    @Column(name = "email_verification_expires_at")
    private Instant emailVerificationExpiresAt;

    @Column(name = "token_invalidation_timestamp")
    private Instant tokenInvalidationTimestamp;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (!active) active = true;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}