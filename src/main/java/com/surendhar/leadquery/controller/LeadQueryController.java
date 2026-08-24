package com.surendhar.leadquery.controller;

import com.surendhar.leadquery.dto.QueryLeadsRequest;
import com.surendhar.leadquery.security.CurrentUser;
import com.surendhar.leadquery.security.CurrentUserContext;
import com.surendhar.leadquery.service.LeadQueryValidator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadQueryController {

    private final LeadQueryValidator validator;
    private final CurrentUserContext currentUserContext;

    public LeadQueryController(
            LeadQueryValidator validator,
            CurrentUserContext currentUserContext
    ) {
        this.validator = validator;
        this.currentUserContext = currentUserContext;
    }

    @PostMapping("/query")
    public CurrentUser queryLeads(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestBody(required = false) QueryLeadsRequest request
    ) {

        validator.validate(
                page,
                limit,
                sortBy,
                sortDirection,
                request
        );

        return currentUserContext.getCurrentUser();
    }
}