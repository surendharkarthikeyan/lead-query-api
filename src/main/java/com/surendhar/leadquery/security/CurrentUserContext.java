package com.surendhar.leadquery.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserContext {

    private final HttpServletRequest request;

    public CurrentUserContext(HttpServletRequest request) {
        this.request = request;
    }

    public CurrentUser getCurrentUser() {

        return (CurrentUser) request.getAttribute("currentUser");
    }
}