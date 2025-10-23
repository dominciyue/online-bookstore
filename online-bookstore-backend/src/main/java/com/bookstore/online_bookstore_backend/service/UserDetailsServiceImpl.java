package com.bookstore.online_bookstore_backend.service;

// import com.bookstore.online_bookstore_backend.dao.UserDao; // No longer directly using UserDao if UserRepository is sufficient
import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.repository.UserRepository; // Import UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    // @Autowired
    // UserDao userDao; // Switched to UserRepository for direct use of Spring Data JPA features

    @Autowired
    UserRepository userRepository; // Inject UserRepository directly

    @Override
    @Transactional(readOnly = true) // readOnly = true is fine as we are fetching data
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Use the new method that fetches UserAuth eagerly
        User user = userRepository.findByUsernameWithUserAuth(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // The User entity itself implements UserDetails, and its getPassword() now delegates to UserAuth.
        // Spring Security will use the getAuthorities() and isEnabled() from our User entity.
        return user;
    }
} 