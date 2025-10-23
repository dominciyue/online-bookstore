package com.bookstore.online_bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false) // Final change: set to false for data integrity
    private UserAuth userAuth;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 20) // Example size constraint
    private String phone;

    @Lob // For potentially longer text
    private String address;

    @Size(max = 255) // Store URL to avatar image
    private String avatarUrl;

    @Column(nullable = false) // Reflecting the 'enabled bit(1) NOT NULL' from DB
    private boolean enabled = true; // Default to true for new users

    @ManyToMany(fetch = FetchType.EAGER) // EAGER fetch for roles as they are needed for authentication/authorization
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        // Delegate to UserAuth for password
        return (this.userAuth != null) ? this.userAuth.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or add a field to manage this
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or add a field to manage this
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or add a field to manage this
    }

    @Override
    public boolean isEnabled() {
        return this.enabled; // Reflect the actual 'enabled' field state
    }

    // Constructor updated - no longer takes password
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.enabled = true; // Ensure new users are enabled by default
    }
} 