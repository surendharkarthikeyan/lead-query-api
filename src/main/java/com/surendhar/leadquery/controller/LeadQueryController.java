package com.surendhar.leadquery.controller;

import com.surendhar.leadquery.dto.LeadResponse;
import com.surendhar.leadquery.dto.QueryLeadsRequest;
import com.surendhar.leadquery.service.LeadQueryService;
import com.surendhar.leadquery.service.LeadQueryValidator;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadQueryController {

    private final LeadQueryValidator validator;
    private final LeadQueryService leadQueryService;

    public LeadQueryController(
            LeadQueryValidator validator,
            LeadQueryService leadQueryService
    ) {
        this.validator = validator;
        this.leadQueryService = leadQueryService;
    }

    @PostMapping("/query")
    public List<LeadResponse> queryLeads(
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

        return leadQueryService.queryLeads(
                page,
                limit,
                sortBy,
                sortDirection,
                request
        );
    }
}