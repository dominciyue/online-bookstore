package com.bookstore.online_bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_auths")
@Getter
@Setter
@NoArgsConstructor
public class UserAuth {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false)
    private String password;

    // Constructor
    public UserAuth(User user, String password) {
        this.user = user;
        this.userId = user.getId();
        this.password = password;
    }
} 