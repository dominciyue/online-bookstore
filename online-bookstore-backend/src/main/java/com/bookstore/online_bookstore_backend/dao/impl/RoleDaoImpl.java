package com.bookstore.online_bookstore_backend.dao.impl;

import com.bookstore.online_bookstore_backend.dao.RoleDao;
import com.bookstore.online_bookstore_backend.entity.ERole;
import com.bookstore.online_bookstore_backend.entity.Role;
import com.bookstore.online_bookstore_backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleDaoImpl implements RoleDao {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleDaoImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findByName(ERole name) {
        return roleRepository.findByName(name);
    }
} 