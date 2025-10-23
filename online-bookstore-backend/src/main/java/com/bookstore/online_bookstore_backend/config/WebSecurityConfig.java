package com.bookstore.online_bookstore_backend.config;

import com.bookstore.online_bookstore_backend.security.jwt.AuthEntryPointJwt;
import com.bookstore.online_bookstore_backend.security.jwt.AuthTokenFilter;
import com.bookstore.online_bookstore_backend.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity // Tells Spring this is a web security configuration
@EnableMethodSecurity // Enables method-level security (e.g., @PreAuthorize)
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disable CSRF as we are using JWT (stateless)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Apply CORS configuration
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Allow access to auth endpoints
                .requestMatchers("/api/books/**").permitAll() // Allow public access to view books (adjust as needed)
                .requestMatchers("/uploads/avatars/**").permitAll() // Allow public access to uploaded avatars
                .requestMatchers("/ws/**").permitAll() // Allow WebSocket connections (authentication handled by WebSocketAuthInterceptor)
                .requestMatchers("/api/users/me").authenticated() // Allow authenticated users to get their details
                .requestMatchers("/api/users/profile").authenticated() // Allow authenticated users to update their profile
                .requestMatchers("/api/users/avatar").authenticated() // Avatar upload endpoint itself needs auth
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Secure admin endpoints
                .requestMatchers("/api/transfer-test/**").permitAll() // Allow access to transfer test endpoints
                .requestMatchers("/api/transaction-test/**").permitAll() // Allow access to transaction test endpoints
                // Add other /api/users/** endpoints here as needed, e.g., for avatar upload
                // .requestMatchers("/api/test/**").permitAll() // Example for public test endpoints
                .anyRequest().authenticated() // All other requests require authentication
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    // This CORS configuration bean might replace or complement your existing WebConfig
    // Ensure it's aligned with your needs (e.g., allowedOrigins from WebConfig)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // Match your frontend
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Apply to /api path
        source.registerCorsConfiguration("/uploads/avatars/**", configuration); // Also apply CORS to avatar paths if needed, though GET usually is fine
        source.registerCorsConfiguration("/ws/**", configuration); // Apply CORS to WebSocket endpoint
        
        return source;
    }
} 