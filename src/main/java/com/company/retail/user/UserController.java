package com.company.retail.user;

import com.company.retail.user.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 🔹 Only ADMIN or SUPERADMIN can view all users
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @GetMapping
    public ResponseEntity<List<UserModel>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * 🔹 All authenticated users can view their own profile
     * 🔹 Admins can view any user
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'CASHIER', 'SUPERVISOR')")
    @GetMapping("/{id}")
    public ResponseEntity<UserModel> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * 🔹 Only ADMIN or SUPERADMIN can create users
     * ✅ Uses DTO (UserRequest) to safely accept shopId instead of nested object
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PostMapping
    public ResponseEntity<UserModel> createUser(@RequestBody UserRequest req) {
        return ResponseEntity.ok(userService.createUser(req));
    }

    /**
     * 🔹 Only ADMIN or SUPERADMIN can update user roles or details
     * ✅ Uses DTO (UserRequest) to safely accept shopId instead of nested object
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserModel> updateUser(@PathVariable Long id, @RequestBody UserRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    /**
     * 🔹 Only ADMIN or SUPERADMIN can delete users
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 🔓 Public login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            Map<String, Object> response = userService.login(username, password);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔹 Authenticated logout endpoint
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'SUPERVISOR', 'CASHIER')")
    @PostMapping("/logout/{id}")
    public ResponseEntity<Void> logout(@PathVariable Long id) {
        userService.logout(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        String tempPassword = userService.resetPassword(id);
        return ResponseEntity.ok(Map.of("temporaryPassword", tempPassword));
    }
}