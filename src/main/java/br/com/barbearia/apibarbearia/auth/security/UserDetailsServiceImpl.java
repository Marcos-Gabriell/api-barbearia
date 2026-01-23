package br.com.barbearia.apibarbearia.auth.security;

import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;

        try {
            Long userId = Long.parseLong(username);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com ID: " + username));
        } catch (NumberFormatException e) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + username));
        }

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Usuário inativo");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getId()))
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}