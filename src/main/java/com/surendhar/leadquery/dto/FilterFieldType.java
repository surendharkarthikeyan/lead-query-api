package com.surendhar.leadquery.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum FilterFieldType {

    STRING,
    NUMBER,
    DATE,
    BOOLEAN;

    @JsonCreator
    public static FilterFieldType fromValue(String value) {

        if (value == null) {
            return null;
        }

        return FilterFieldType.valueOf(
                value.toUpperCase(Locale.ROOT)
        );
    }
}