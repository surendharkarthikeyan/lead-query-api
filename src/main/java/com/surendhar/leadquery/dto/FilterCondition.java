package com.surendhar.leadquery.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum FilterCondition {

    IS,
    IS_NOT,
    CONTAIN,
    DOES_NOT_CONTAIN,
    STARTS_WITH,
    ENDS_WITH,
    BEFORE,
    AFTER,
    GREATER_THAN,
    LESS_THAN,
    IS_EMPTY,
    IS_NOT_EMPTY;

    @JsonCreator
    public static FilterCondition fromValue(String value) {

        if (value == null) {
            return null;
        }

        return FilterCondition.valueOf(
                value
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "_")
        );
    }
}