package com.bookstore.online_bookstore_backend.dao;

import com.bookstore.online_bookstore_backend.entity.ERole;
import com.bookstore.online_bookstore_backend.entity.Role;
import java.util.Optional;

public interface RoleDao {
    Optional<Role> findByName(ERole name);
    // Add other RoleRepository methods if they are used by services
} 