package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.dao.RoleDao;
import com.bookstore.online_bookstore_backend.dao.UserDao;
import com.bookstore.online_bookstore_backend.entity.ERole;
import com.bookstore.online_bookstore_backend.entity.Role;
import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.entity.UserAuth;
import com.bookstore.online_bookstore_backend.payload.request.LoginRequest;
import com.bookstore.online_bookstore_backend.payload.request.SignupRequest;
import com.bookstore.online_bookstore_backend.payload.response.JwtResponse;
import com.bookstore.online_bookstore_backend.payload.response.LogoutResponse;
import com.bookstore.online_bookstore_backend.payload.response.MessageResponse;
import com.bookstore.online_bookstore_backend.security.jwt.JwtUtils;
import com.bookstore.online_bookstore_backend.service.TimerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600) // Or configure more specifically via WebSecurityConfig
@RestController
@RequestMapping("/api/auth")
@SessionScope
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserDao userDao;

    @Autowired
    RoleDao roleDao;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    TimerService timerService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        System.out.println("=== AUTH DEBUG: Login ===");
        System.out.println("User details: " + userDetails);
        System.out.println("User ID: " + userDetails.getId());
        System.out.println("About to call timerService.startTimer...");
        timerService.startTimer(userDetails.getId());
        System.out.println("timerService.startTimer() called successfully");
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userDao.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userDao.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account - constructor no longer takes password
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleDao.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleDao.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                    case "moderator":
                        Role modRole = roleDao.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role MODERATOR is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleDao.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);

        // Create and associate UserAuth
        UserAuth userAuth = new UserAuth();
        userAuth.setPassword(encoder.encode(signUpRequest.getPassword()));
        // The UserAuth ID will be set from User when User is persisted if @MapsId is used correctly
        // and UserAuth.user is set. And User.userAuth is set for cascading.
        
        // Establish bidirectional relationship
        user.setUserAuth(userAuth); // This is crucial for cascade persist
        userAuth.setUser(user);     // This ensures UserAuth.user is set for @MapsId and FK

        userDao.save(user); // This should save User and cascade to save UserAuth due to CascadeType.ALL on User.userAuth

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("=== BACKEND LOGOUT ===");
        System.out.println("User details: " + userDetails);
        System.out.println("User ID: " + userDetails.getId());
        System.out.println("Username: " + userDetails.getUsername());

        long sessionDuration = timerService.stopTimer(userDetails.getId());
        System.out.println("=== BACKEND LOGOUT DEBUG ===");
        System.out.println("Session duration calculated: " + sessionDuration + "ms");

        LogoutResponse response = new LogoutResponse("User logged out successfully!", sessionDuration);
        System.out.println("LogoutResponse created: " + response);
        System.out.println("Response message: " + response.getMessage());
        System.out.println("Response sessionDuration: " + response.getSessionDuration());

        return ResponseEntity.ok(response);
    }
} 