package com.careconnect.config;

import com.careconnect.model.AppUser;
import com.careconnect.repository.AppUserRepository;
import com.careconnect.service.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class AdminBootstrap {
    @Bean
    CommandLineRunner bootstrapAdmin(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            AuditService audit,
            @Value("${careconnect.bootstrap-admin.username:}") String username,
            @Value("${careconnect.bootstrap-admin.password:}") String password,
            @Value("${careconnect.bootstrap-admin.display-name:System Administrator}") String displayName) {
        return args -> {
            if (username.isBlank() || password.isBlank() || users.findByUsername(username).isPresent()) return;
            if (password.length() < 8) throw new IllegalStateException("CARECONNECT_BOOTSTRAP_ADMIN_PASSWORD must be at least 8 characters");
            AppUser user = new AppUser();
            user.username = username;
            user.displayName = displayName;
            user.role = "ADMIN";
            user.initials = initials(displayName);
            user.subtitle = "System administration";
            user.passwordHash = passwordEncoder.encode(password);
            user.active = true;
            user = users.save(user);
            audit.record(new DemoPrincipal(user.id, user.username, user.role, null), "ADMIN_BOOTSTRAPPED", "AppUser", user.id, "Initial administrator account provisioned");
        };
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "")).toUpperCase();
    }
}
