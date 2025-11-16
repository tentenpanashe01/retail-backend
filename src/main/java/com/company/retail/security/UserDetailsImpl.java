package com.company.retail.security;

import com.company.retail.user.UserModel;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final UserModel user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<UserModel.Role> roles = user.getRoles();
        if (roles == null) return Set.of();
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"DISABLED".equalsIgnoreCase(user.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !"DISABLED".equalsIgnoreCase(user.getStatus());
    }

    public String getFullName() {
        return user.getFullName();
    }

    public Long getUserId() {
        return user.getUserId();
    }
}