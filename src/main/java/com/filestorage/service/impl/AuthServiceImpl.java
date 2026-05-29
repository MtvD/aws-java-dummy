package com.filestorage.service.impl;

import com.filestorage.dto.AuthResponse;
import com.filestorage.dto.LoginRequest;
import com.filestorage.dto.RegisterRequest;
import com.filestorage.exception.InvalidCredentialsException;
import com.filestorage.model.User;
import com.filestorage.repository.UserRepository;
import com.filestorage.security.JwtProvider;
import com.filestorage.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        user = userRepository.save(user);

        String token = jwtProvider.generateToken(user.getId(), user.getUsername());
        LocalDateTime expiresAt = jwtProvider.getExpirationFromToken(token)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        return new AuthResponse(token, expiresAt);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtProvider.generateToken(user.getId(), user.getUsername());
        LocalDateTime expiresAt = jwtProvider.getExpirationFromToken(token)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        return new AuthResponse(token, expiresAt);
    }
}
