package com.company.retail.user;

import com.company.retail.security.JwtService;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import com.company.retail.user.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * ✅ Get all users
     */
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * ✅ Get user by ID
     */
    public UserModel getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * ✅ Create new user using DTO (UserRequest)
     */
    public UserModel createUser(UserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        UserModel user = new UserModel();
        user.setFullName(req.getFullName());
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setStatus("Active");
        user.setCreatedAt(LocalDateTime.now());

        // ✅ Convert String roles from request into Enum roles
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            Set<UserModel.Role> roleSet = new HashSet<>();
            for (String role : req.getRoles()) {
                try {
                    roleSet.add(UserModel.Role.valueOf(role));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid role: " + role);
                }
            }
            user.setRoles(roleSet);
        } else {
            user.setRoles(Set.of(UserModel.Role.ROLE_CASHIER));
        }

        // ✅ Assign shop if provided (optional for ADMIN/SUPERADMIN)
        if (req.getShopId() != null) {
            ShopModel shop = shopRepository.findById(req.getShopId())
                    .orElseThrow(() -> new RuntimeException("Shop not found"));
            user.setShop(shop);
        } else {
            user.setShop(null);
        }

        return userRepository.save(user);
    }

    /**
     * ✅ Update user (using DTO)
     */
    public UserModel updateUser(Long id, UserRequest req) {
        UserModel existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existing.setFullName(req.getFullName());
        existing.setUsername(req.getUsername());
        existing.setStatus("Active");

        // ✅ Update password if provided
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        // ✅ Update roles
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            Set<UserModel.Role> roleSet = new HashSet<>();
            for (String role : req.getRoles()) {
                try {
                    roleSet.add(UserModel.Role.valueOf(role));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid role: " + role);
                }
            }
            existing.setRoles(roleSet);
        }

        // ✅ Update shop if applicable
        if (req.getShopId() != null) {
            ShopModel shop = shopRepository.findById(req.getShopId())
                    .orElseThrow(() -> new RuntimeException("Shop not found"));
            existing.setShop(shop);
        } else {
            existing.setShop(null);
        }

        return userRepository.save(existing);
    }

    /**
     * ✅ Delete user
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * ✅ Login - validate credentials, return JWT token & user details
     */
    public Map<String, Object> login(String username, String password) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // ✅ Generate JWT
        String token = jwtService.generateToken(user);

        user.setStatus("Online");
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("roles", user.getRoles());
        response.put("userId", user.getUserId());

        // ✅ Add these lines to ensure shop info is returned
        if (user.getShop() != null) {
            response.put("shopId", user.getShop().getId());
            response.put("shopName", user.getShop().getShopName());
        } else {
            response.put("shopId", null);
            response.put("shopName", "System-wide");
        }

        return response;

    }

    /**
     * ✅ Logout (set status offline)
     */
    public void logout(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus("Offline");
            userRepository.save(user);
        });
    }

   /*
   * Reset password
   * */
    public String resetPassword(Long id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        return tempPassword;
    }
}