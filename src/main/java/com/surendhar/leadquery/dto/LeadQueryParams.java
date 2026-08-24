package com.surendhar.leadquery.dto;

public record LeadQueryParams(

        int page,

        int limit,

        String sortBy,

        String sortDirection

) {
}