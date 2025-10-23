package com.bookstore.online_bookstore_backend.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        System.out.println("<<<< DEBUG_PRINT_SYSTEM_OUT: AuthEntryPointJwt.commence ENTERED for path: " + request.getServletPath() + " with exception: " + authException.getMessage() + " >>>>");
        logger.error("Unauthorized error: {} for path: {}", authException.getMessage(), request.getServletPath());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        String message = authException.getMessage() != null ? authException.getMessage() : "Authentication failed";
        body.put("message", message);
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(response.getOutputStream(), body);
            System.out.println("<<<< DEBUG_PRINT_SYSTEM_OUT: AuthEntryPointJwt.commence - JSON response written for path: " + request.getServletPath() + " >>>>");
        } catch (Exception e) {
            System.err.println("<<<< DEBUG_PRINT_SYSTEM_OUT: AuthEntryPointJwt.commence - ERROR writing JSON response: " + e.getMessage() + " for path: " + request.getServletPath() + " >>>>");
            logger.error("Error writing JSON response in AuthEntryPointJwt for path {}: {}", request.getServletPath(), e.getMessage(), e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error generating authentication error response.");
            }
            return;
        }
        System.out.println("<<<< DEBUG_PRINT_SYSTEM_OUT: AuthEntryPointJwt.commence EXITED for path: " + request.getServletPath() + " >>>>");
    }
}
