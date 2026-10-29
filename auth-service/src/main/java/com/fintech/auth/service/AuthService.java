package com.fintech.auth.service;

import com.fintech.auth.dto.AuthResponse;
import com.fintech.auth.dto.LoginRequest;
import com.fintech.auth.dto.OtpChallengeResponse;
import com.fintech.auth.dto.OtpRequest;
import com.fintech.auth.dto.RegisterRequest;
import com.fintech.auth.dto.RegistrationResponse;
import com.fintech.auth.entity.Role;
import com.fintech.auth.entity.RoleName;
import com.fintech.auth.entity.User;
import com.fintech.auth.mapper.UserMapper;
import com.fintech.auth.repository.RoleRepository;
import com.fintech.auth.repository.UserRepository;
import com.fintech.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String otpCode;
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    public AuthService(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder,
                       JwtService jwtService, @Value("${security.otp.code}") String otpCode) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpCode = otpCode;
    }

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà inscrite");
        }
        Role role = roles.findByName(RoleName.CLIENT).orElseGet(() -> roles.save(new Role(RoleName.CLIENT)));
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(role));
        return UserMapper.toResponse(users.save(user));
    }

    public OtpChallengeResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        // Le challenge est conservé en mémoire pour cette démonstration ; en production, utiliser un stockage partagé avec expiration.
        challenges.put(user.getEmail(), new Challenge(otpCode, Instant.now().plusSeconds(300)));
        return new OtpChallengeResponse(user.getEmail(), true, "Code OTP requis pour ce challenge");
    }

    public AuthResponse verifyOtp(OtpRequest request) {
        String email = request.email().trim().toLowerCase();
        Challenge challenge = challenges.get(email);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now()) || !challenge.code().equals(request.code())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Code OTP invalide ou expiré");
        }
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
        challenges.remove(email);
        String role = user.getRoles().iterator().next().getName().name();
        return new AuthResponse(email, role, "Bearer", jwtService.generateToken(email, role, user.getId().toString()));
    }

    private record Challenge(String code, Instant expiresAt) {
    }
}