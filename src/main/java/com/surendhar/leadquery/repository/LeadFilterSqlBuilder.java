package com.surendhar.leadquery.repository;

import com.surendhar.leadquery.dto.FilterCondition;
import com.surendhar.leadquery.dto.LeadFilter;
import com.surendhar.leadquery.util.SystemFieldMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LeadFilterSqlBuilder {

    public static String buildSystemFilter(
            LeadFilter filter,
            List<Object> parameters
    ) {

        String column = SystemFieldMapper.getColumn(
                filter.fieldId()
        );

        if (column == null) {
            return null;
        }

        String value = filter.value();

        return switch (filter.condition()) {

            case IS -> {
                parameters.add(value);
                yield "LOWER(" + column + ") = LOWER(?)";
            }

            case IS_NOT -> {
                parameters.add(value);
                yield "(" +
                        column + " IS NULL OR LOWER(" +
                        column +
                        ") <> LOWER(?)" +
                        ")";
            }

            case CONTAIN -> {
                parameters.add("%" + value + "%");
                yield column + " ILIKE ?";
            }

            case DOES_NOT_CONTAIN -> {
                parameters.add("%" + value + "%");
                yield "(" +
                        column +
                        " IS NULL OR " +
                        column +
                        " NOT ILIKE ?" +
                        ")";
            }

            case STARTS_WITH -> {
                parameters.add(value + "%");
                yield column + " ILIKE ?";
            }

            case ENDS_WITH -> {
                parameters.add("%" + value);
                yield column + " ILIKE ?";
            }

            case IS_EMPTY ->
                    "(" +
                            column +
                            " IS NULL OR " +
                            column +
                            " = ''" +
                            ")";

            case IS_NOT_EMPTY ->
                    "(" +
                            column +
                            " IS NOT NULL AND " +
                            column +
                            " <> ''" +
                            ")";

            default -> null;
        };
    }

    public static String buildCustomFilter(
            LeadFilter filter,
            List<Object> parameters
    ) {

        String fieldId = filter.fieldId();
        String value = filter.value();

        return switch (filter.condition()) {

            case CONTAIN -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add("%" + value + "%");

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND lcfv.value ILIKE ?
                    )
                    """;
            }

            case IS -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(value);

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND LOWER(lcfv.value) = LOWER(?)
                    )
                    """;
            }

            case IS_NOT -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(value);

                yield """
                    NOT EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND LOWER(lcfv.value) = LOWER(?)
                    )
                    """;
            }

            case IS_EMPTY -> {
                parameters.add(UUID.fromString(fieldId));

                yield """
                    NOT EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND lcfv.value IS NOT NULL
                          AND lcfv.value <> ''
                    )
                    """;
            }

            case IS_NOT_EMPTY -> {
                parameters.add(UUID.fromString(fieldId));

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND lcfv.value IS NOT NULL
                          AND lcfv.value <> ''
                    )
                    """;
            }

            case GREATER_THAN -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(new java.math.BigDecimal(value));

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND CAST(lcfv.value AS NUMERIC) > ?
                    )
                    """;
            }

            case LESS_THAN -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(new java.math.BigDecimal(value));

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND CAST(lcfv.value AS NUMERIC) < ?
                    )
                    """;
            }

            case DOES_NOT_CONTAIN -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add("%" + value + "%");

                yield """
                    NOT EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND lcfv.value ILIKE ?
                    )
                    """;
            }

            case STARTS_WITH -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(value + "%");

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND lcfv.value ILIKE ?
                    )
                    """;
            }

            case ENDS_WITH -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add("%" + value);

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND lcfv.value ILIKE ?
                    )
                    """;
            }

            case BEFORE -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(LocalDate.parse(value));

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND CAST(lcfv.value AS DATE) < ?
                    )
                    """;
            }

            case AFTER -> {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(LocalDate.parse(value));

                yield """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND CAST(lcfv.value AS DATE) > ?
                    )
                    """;
            }

            default -> null;
        };
    }
}