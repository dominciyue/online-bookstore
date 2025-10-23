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
    // This ID will be populated with the User's ID
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId // Indicates that the primary key of this entity is also a foreign key to User
    @JoinColumn(name = "user_id") // Specifies the foreign key column in the user_auths table
    private User user;

    @NotBlank
    @Size(max = 120) // Consistent with previous password size in User entity
    @Column(nullable = false)
    private String password;

    // Constructor, for example, to be used in service layer
    public UserAuth(User user, String password) {
        this.user = user;
        this.id = user.getId(); // Set the ID from the associated User
        this.password = password;
    }
} 