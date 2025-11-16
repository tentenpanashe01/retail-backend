package com.company.retail.configs;

import com.company.retail.user.UserModel;
import com.company.retail.user.UserModel.Role;
import com.company.retail.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // 🔹 Check if default admin already exists
        userRepository.findByUsername("admin").ifPresentOrElse(
                user -> System.out.println("ℹ️ Admin user already exists, skipping initialization."),
                () -> {
                    // 🔸 Create admin user
                    UserModel admin = UserModel.builder()
                            .username("admin")
                            .fullName("System Administrator")
                            .password(passwordEncoder.encode("admin123")) // ✅ Hash password
                            .email("admin@retailsystem.com")
                            .phoneNumber("+263000000000")
                            .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_SUPERADMIN)) // ✅ Assign roles
                            .status("Active")
                            .createdAt(LocalDateTime.now())
                            .build();

                    userRepository.save(admin);
                    System.out.println("✅ Default admin user created (username: admin / password: admin123)");
                }
        );
    }
}