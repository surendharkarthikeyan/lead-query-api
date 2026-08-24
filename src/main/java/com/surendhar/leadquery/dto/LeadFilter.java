package com.surendhar.leadquery.dto;

public record LeadFilter(

        String fieldId,

        FilterFieldType fieldType,

        FilterCondition condition,

        String value,

        String inputType

) {
}