package com.surendhar.leadquery.security;

public record CurrentUser(
        String tenantId,
        String userId,
        String role
) {
}