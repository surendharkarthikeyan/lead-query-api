package com.surendhar.leadquery.service;

import com.surendhar.leadquery.dto.FilterCondition;
import com.surendhar.leadquery.dto.FilterFieldType;
import com.surendhar.leadquery.dto.LeadFilter;
import com.surendhar.leadquery.dto.QueryLeadsRequest;
import com.surendhar.leadquery.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class LeadQueryValidator {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "followUpDate"
    );

    private static final Set<String> ALLOWED_SORT_DIRECTIONS = Set.of(
            "asc",
            "desc"
    );

    private static final Set<String> ALLOWED_LOGIC = Set.of(
            "AND",
            "OR"
    );

    public void validate(
            int page,
            int limit,
            String sortBy,
            String sortDirection,
            QueryLeadsRequest request
    ) {

        validatePagination(page, limit);
        validateSorting(sortBy, sortDirection);
        validateRequest(request);
    }

    private void validatePagination(int page, int limit) {

        if (page < 1) {
            throw new BadRequestException(
                    "page must be greater than or equal to 1"
            );
        }

        if (limit < 1 || limit > 100) {
            throw new BadRequestException(
                    "limit must be between 1 and 100"
            );
        }
    }

    private void validateSorting(
            String sortBy,
            String sortDirection
    ) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException(
                    "sortBy must be \"followUpDate\" or \"createdAt\""
            );
        }

        if (!ALLOWED_SORT_DIRECTIONS.contains(
                sortDirection.toLowerCase(Locale.ROOT)
        )) {

            throw new BadRequestException(
                    "sortDirection must be \"asc\" or \"desc\""
            );
        }
    }

    private void validateRequest(QueryLeadsRequest request) {

        if (request == null) {
            return;
        }

        validateLogic(request.logic());

        if (request.filters() == null) {
            return;
        }

        for (LeadFilter filter : request.filters()) {
            validateFilter(filter);
        }
    }

    private void validateLogic(String logic) {

        if (logic == null || logic.isBlank()) {
            return;
        }

        if (!ALLOWED_LOGIC.contains(
                logic.toUpperCase(Locale.ROOT)
        )) {

            throw new BadRequestException(
                    "logic must be \"AND\" or \"OR\""
            );
        }
    }

    private void validateFilter(LeadFilter filter) {

        if (filter == null) {
            throw new BadRequestException(
                    "filter cannot be null"
            );
        }

        if (filter.fieldId() == null || filter.fieldId().isBlank()) {
            throw new BadRequestException(
                    "fieldId is required"
            );
        }

        if (filter.fieldType() == null) {
            throw new BadRequestException(
                    "fieldType is required"
            );
        }

        if (filter.condition() == null) {
            throw new BadRequestException(
                    "condition is required"
            );
        }

        validateCondition(
                filter.fieldType(),
                filter.condition()
        );

        validateValue(filter);
    }

    private void validateCondition(
            FilterFieldType fieldType,
            FilterCondition condition
    ) {

        boolean valid = switch (fieldType) {

            case STRING -> Set.of(
                    FilterCondition.IS,
                    FilterCondition.IS_NOT,
                    FilterCondition.CONTAIN,
                    FilterCondition.DOES_NOT_CONTAIN,
                    FilterCondition.STARTS_WITH,
                    FilterCondition.ENDS_WITH,
                    FilterCondition.IS_EMPTY,
                    FilterCondition.IS_NOT_EMPTY
            ).contains(condition);

            case NUMBER -> Set.of(
                    FilterCondition.IS,
                    FilterCondition.GREATER_THAN,
                    FilterCondition.LESS_THAN,
                    FilterCondition.IS_EMPTY,
                    FilterCondition.IS_NOT_EMPTY
            ).contains(condition);

            case DATE -> Set.of(
                    FilterCondition.IS,
                    FilterCondition.BEFORE,
                    FilterCondition.AFTER,
                    FilterCondition.IS_EMPTY,
                    FilterCondition.IS_NOT_EMPTY
            ).contains(condition);

            case BOOLEAN -> condition == FilterCondition.IS;
        };

        if (!valid) {
            throw new BadRequestException(
                    "Condition \"" + condition +
                            "\" is not supported for field type \"" +
                            fieldType + "\""
            );
        }
    }

    private void validateValue(LeadFilter filter) {

        FilterCondition condition = filter.condition();

        boolean doesNotNeedValue =
                condition == FilterCondition.IS_EMPTY ||
                        condition == FilterCondition.IS_NOT_EMPTY;

        if (doesNotNeedValue) {
            return;
        }

        if (filter.value() == null || filter.value().isBlank()) {
            throw new BadRequestException(
                    "value is required for condition \"" +
                            condition + "\""
            );
        }

        switch (filter.fieldType()) {

            case NUMBER -> validateNumber(filter.value());

            case DATE -> validateDate(filter.value());

            case BOOLEAN -> validateBoolean(filter.value());

            case STRING -> {
                // No additional conversion required.
            }
        }
    }

    private void validateNumber(String value) {

        try {
            Double.parseDouble(value);
        } catch (NumberFormatException exception) {

            throw new BadRequestException(
                    "Invalid number value: " + value
            );
        }
    }

    private void validateDate(String value) {

        try {
            LocalDate.parse(value);
        } catch (DateTimeParseException exception) {

            throw new BadRequestException(
                    "Invalid date. Expected format: YYYY-MM-DD"
            );
        }
    }

    private void validateBoolean(String value) {

        if (!value.equalsIgnoreCase("true")
                && !value.equalsIgnoreCase("false")) {

            throw new BadRequestException(
                    "Boolean value must be true or false"
            );
        }
    }
}