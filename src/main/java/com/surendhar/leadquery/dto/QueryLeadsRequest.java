package com.surendhar.leadquery.dto;

import java.util.List;

public record QueryLeadsRequest(

        String q,

        String logic,

        List<LeadFilter> filters

) {
}