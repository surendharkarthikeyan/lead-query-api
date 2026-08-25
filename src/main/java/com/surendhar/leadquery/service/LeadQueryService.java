package com.surendhar.leadquery.service;

import com.surendhar.leadquery.dto.LeadFilter;
import com.surendhar.leadquery.dto.LeadQueryResponse;
import com.surendhar.leadquery.dto.QueryLeadsRequest;
import com.surendhar.leadquery.repository.LeadQueryRepository;
import com.surendhar.leadquery.security.CurrentUserContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LeadQueryService {

    private final LeadQueryRepository leadQueryRepository;
    private final CurrentUserContext currentUserContext;

    public LeadQueryService(
            LeadQueryRepository leadQueryRepository,
            CurrentUserContext currentUserContext
    ) {
        this.leadQueryRepository = leadQueryRepository;
        this.currentUserContext = currentUserContext;
    }

        public LeadQueryResponse queryLeads(
            int page,
            int limit,
            String sortBy,
            String sortDirection,
            QueryLeadsRequest request
    ) {

        /*
         * ---------------------------------------------------------
         * 1. Get tenant from simulated authenticated user
         * ---------------------------------------------------------
         */

        UUID tenantId = UUID.fromString(
                currentUserContext
                        .getCurrentUser()
                        .tenantId()
        );


        /*
         * ---------------------------------------------------------
         * 2. Calculate pagination offset
         * ---------------------------------------------------------
         *
         * page 1, limit 20 -> offset 0
         * page 2, limit 20 -> offset 20
         * page 3, limit 20 -> offset 40
         */

        int offset = (page - 1) * limit;


        /*
         * ---------------------------------------------------------
         * 3. Extract request values
         * ---------------------------------------------------------
         */

        String q = request != null
                ? request.q()
                : null;

        List<LeadFilter> filters = request != null
                ? request.filters()
                : null;

        String logic = request != null && request.logic() != null
                ? request.logic()
                : "AND";


        /*
         * ---------------------------------------------------------
         * 4. Query repository
         * ---------------------------------------------------------
         */

        LeadQueryRepository.LeadQueryResult result =
                leadQueryRepository.findLeadsByTenant(
                        tenantId,
                        UUID.fromString(currentUserContext.getCurrentUser().userId()),
                        currentUserContext.getCurrentUser().role(),
                        limit,
                        offset,
                        sortBy,
                        sortDirection,
                        q,
                        filters,
                        logic
                );

        int totalPages = (int) Math.ceil(
                (double) result.totalRecords() / limit
        );

        return new LeadQueryResponse(
                "success",
                "Leads fetched successfully",
                result.data(),
                new LeadQueryResponse.Meta(
                        page,
                        limit,
                        result.totalRecords(),
                        totalPages
                )
        );
    }
}