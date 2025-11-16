package com.company.retail.authentication;

import com.company.retail.security.JwtService;
import com.company.retail.user.UserModel;
import com.company.retail.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // 🔹 Register user
    @PostMapping("/register")
    public UserModel register(@RequestBody UserModel user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus("Active");
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // 🔹 Login and return JWT
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("roles", user.getRoles());
        return response;
    }
}