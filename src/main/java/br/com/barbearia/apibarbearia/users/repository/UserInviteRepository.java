package br.com.barbearia.apibarbearia.users.repository;

import br.com.barbearia.apibarbearia.users.entity.UserInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserInviteRepository extends JpaRepository<UserInvite, Long> {
    Optional<UserInvite> findByToken(String token);

    List<UserInvite> findAllByEmail(String email);

    Optional<UserInvite> findTopByEmailOrderByCreatedAtDesc(String email);
}