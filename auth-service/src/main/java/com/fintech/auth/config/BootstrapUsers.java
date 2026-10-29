package com.fintech.auth.config;

import com.fintech.auth.entity.Role;
import com.fintech.auth.entity.RoleName;
import com.fintech.auth.entity.User;
import com.fintech.auth.repository.RoleRepository;
import com.fintech.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Component
public class BootstrapUsers implements ApplicationRunner {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String analystEmail;
    private final String analystPassword;

    public BootstrapUsers(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder,
                          @Value("${security.bootstrap.admin-email:}") String adminEmail,
                          @Value("${security.bootstrap.admin-password:}") String adminPassword,
                          @Value("${security.bootstrap.analyst-email:}") String analystEmail,
                          @Value("${security.bootstrap.analyst-password:}") String analystPassword) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.analystEmail = analystEmail;
        this.analystPassword = analystPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfConfigured(adminEmail, adminPassword, RoleName.ADMIN);
        createIfConfigured(analystEmail, analystPassword, RoleName.RISK_ANALYST);
    }

    private void createIfConfigured(String email, String password, RoleName roleName) {
        if (email.isBlank() && password.isBlank()) {
            return;
        }
        if (email.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Le compte de démonstration " + roleName + " exige un e-mail et un mot de passe de 12 caractères minimum");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }
        Role role = roles.findByName(roleName).orElseGet(() -> roles.save(new Role(roleName)));
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRoles(Set.of(role));
        users.save(user);
    }
}