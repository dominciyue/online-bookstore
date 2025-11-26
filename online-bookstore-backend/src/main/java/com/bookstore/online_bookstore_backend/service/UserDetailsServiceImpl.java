package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithUserAuth(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return user;
    }
} 