package com.surendhar.leadquery.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
public class AuthSimulationFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "owner",
            "admin",
            "manager",
            "agent"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String tenantId = request.getHeader("x-tenant-id");
        String userId = request.getHeader("x-user-id");
        String role = request.getHeader("x-user-role");

        // Validate required headers
        if (tenantId == null || tenantId.isBlank()
                || userId == null || userId.isBlank()
                || role == null || role.isBlank()) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                      "message": "Missing authentication headers",
                      "statusCode": 401
                    }
                    """);

            return;
        }

        // Validate UUIDs
        try {
            UUID.fromString(tenantId);
            UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                      "message": "Invalid tenant ID or user ID",
                      "statusCode": 401
                    }
                    """);

            return;
        }

        // Validate role
        if (!ALLOWED_ROLES.contains(role.toLowerCase())) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                      "message": "Invalid user role",
                      "statusCode": 401
                    }
                    """);

            return;
        }

        CurrentUser currentUser = new CurrentUser(
                tenantId,
                userId,
                role.toLowerCase()
        );

        request.setAttribute("currentUser", currentUser);

        filterChain.doFilter(request, response);
    }
}