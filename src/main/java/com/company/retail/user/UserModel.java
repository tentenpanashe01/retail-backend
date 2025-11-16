package com.company.retail.user;

import com.company.retail.shop.ShopModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ShopModel shop;

    private LocalDateTime createdAt;

    public enum Role {
        ROLE_CASHIER,
        ROLE_SUPERVISOR,
        ROLE_ADMIN,
        ROLE_SUPERADMIN
    }

    public UserModel(Long userId) {
        this.userId = userId;
    }
}