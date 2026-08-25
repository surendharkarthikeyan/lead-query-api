package com.surendhar.leadquery.dto;

import java.util.List;

public record LeadQueryResponse(
        String status,
        String message,
        List<LeadResponse> data,
        Meta meta
) {

    public record Meta(
            int page,
            int limit,
            long totalRecords,
            int totalPages
    ) {
    }
}
