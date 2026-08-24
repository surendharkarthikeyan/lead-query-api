package com.surendhar.leadquery.controller;

import com.surendhar.leadquery.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthController {

    @GetMapping("/api/v1/test-auth")
    public CurrentUser testAuth(HttpServletRequest request) {

        return (CurrentUser) request.getAttribute("currentUser");
    }
}