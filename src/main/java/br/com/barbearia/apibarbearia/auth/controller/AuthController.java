package br.com.barbearia.apibarbearia.auth.controller;

import br.com.barbearia.apibarbearia.auth.dto.LoginRequest;
import br.com.barbearia.apibarbearia.auth.dto.LoginResponse;
import br.com.barbearia.apibarbearia.auth.security.JwtService;
import br.com.barbearia.apibarbearia.common.exception.UnauthorizedException;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.email.toLowerCase().trim(),
                            req.password
                    )
            );

            String token = jwtService.generateToken(auth.getName());
            LoginResponse resp = new LoginResponse();
            resp.token = token;
            return resp;

        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid credentials");
        } catch (DisabledException ex) {
            throw new UnauthorizedException("User is disabled");
        }
    }

    @GetMapping("/me")
    public Object me(Authentication authentication) {
        var email = authentication.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        return new Object() {
            public final Long id = user.getId();
            public final String name = user.getName();
            public final String emailAddr = user.getEmail();
            public final String role = user.getRole().name();
            public final boolean active = user.isActive();
        };
    }
}
