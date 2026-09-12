package com.campus.lostfound.service;

import com.campus.lostfound.dto.AuthResponse;
import com.campus.lostfound.dto.LoginRequest;
import com.campus.lostfound.dto.RegisterRequest;
import com.campus.lostfound.entity.Role;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.UserRepository;
import com.campus.lostfound.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.college-domain}")
    private String collegeDomain;

    public AuthResponse register(RegisterRequest request) {
        // 1. Enforce college email rule
        if (!request.email().toLowerCase().endsWith(collegeDomain.toLowerCase())) {
            throw new IllegalArgumentException("Registration is restricted to " + collegeDomain + " emails.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered.");
        }

        // 2. Hash password and save user
        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.STUDENT)
                .build();

        userRepository.save(user);

        // 3. Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        var jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(jwtToken, "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate credentials (throws BadCredentialsException if wrong)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        var jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(jwtToken, "Login successful");
    }
}