package com.surendhar.leadquery.repository;

import com.surendhar.leadquery.dto.LeadFilter;
import com.surendhar.leadquery.dto.FilterFieldType;
import com.surendhar.leadquery.util.SystemFieldMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

        /*
         * UUID system fields
         */
        if (filter.fieldId().equals("assignedTo")
                || filter.fieldId().equals("createdBy")) {

            boolean multiselect = "multiselect".equalsIgnoreCase(
                filter.inputType()
            );

            if (multiselect) {
            List<UUID> ids = parseUuidValues(value);
            String placeholders = String.join(", ", ids.stream()
                .map(id -> "?")
                .toList());

            return switch (filter.condition()) {

                case IS, CONTAIN -> {
                parameters.addAll(ids);
                yield column + " IN (" + placeholders + ")";
                }

                case IS_NOT, DOES_NOT_CONTAIN -> {
                parameters.addAll(ids);
                yield "(" + column + " IS NULL OR " +
                    column + " NOT IN (" + placeholders + "))";
                }

                case IS_EMPTY ->
                    column + " IS NULL";

                case IS_NOT_EMPTY ->
                    column + " IS NOT NULL";

                default -> null;
            };
            }

            return switch (filter.condition()) {

                case IS -> {
                    parameters.add(UUID.fromString(value));
                    yield column + " = ?";
                }

                case IS_NOT -> {
                    parameters.add(UUID.fromString(value));
                    yield "(" +
                            column + " IS NULL OR " +
                            column + " <> ?" +
                            ")";
                }

                case IS_EMPTY ->
                        column + " IS NULL";

                case IS_NOT_EMPTY ->
                        column + " IS NOT NULL";

                default -> null;
            };
        }


        if (filter.fieldId().equals("followUpDate")) {

            return switch (filter.condition()) {

                case IS -> {
                    parameters.add(LocalDate.parse(value));
                    yield column + " = ?";
                }

                case IS_NOT -> {
                    parameters.add(LocalDate.parse(value));
                    yield "(" +
                            column + " IS NULL OR " +
                            column + " <> ?" +
                            ")";
                }

                case BEFORE -> {
                    parameters.add(LocalDate.parse(value));
                    yield column + " < ?";
                }

                case AFTER -> {
                    parameters.add(LocalDate.parse(value));
                    yield column + " > ?";
                }

                case IS_EMPTY ->
                        column + " IS NULL";

                case IS_NOT_EMPTY ->
                        column + " IS NOT NULL";

                default -> null;
            };
        }


        if (filter.fieldId().equals("createdAt")
                || filter.fieldId().equals("updatedAt")) {

            return switch (filter.condition()) {

                case IS -> {
                    OffsetDateTime start = parseTimestamp(value);
                    parameters.add(start);
                    parameters.add(start.plusDays(1));
                    yield column + " >= ? AND " + column + " < ?";
                }

                case IS_NOT -> {
                    OffsetDateTime start = parseTimestamp(value);
                    parameters.add(start);
                    parameters.add(start.plusDays(1));
                    yield "(" +
                            column + " IS NULL OR " +
                            column + " < ? OR " +
                            column + " >= ?" +
                            ")";
                }

                case BEFORE -> {
                    parameters.add(parseTimestamp(value));
                    yield column + " < ?";
                }

                case AFTER -> {
                    parameters.add(parseTimestamp(value));
                    yield column + " > ?";
                }

                case IS_EMPTY ->
                        column + " IS NULL";

                case IS_NOT_EMPTY ->
                        column + " IS NOT NULL";

                default -> null;
            };
        }


        /*
         * Normal text system fields
         */
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

    private static OffsetDateTime parseTimestamp(String value) {
        return value.contains("T")
                ? OffsetDateTime.parse(value)
                : LocalDate.parse(value).atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private static List<UUID> parseUuidValues(String value) {
        return List.of(value.split(",")).stream()
                .map(String::trim)
                .map(UUID::fromString)
                .toList();
    }

    public static String buildCustomFilter(
            LeadFilter filter,
            List<Object> parameters
    ) {

        String fieldId = filter.fieldId();
        String value = filter.value();

        if (filter.condition() == com.surendhar.leadquery.dto.FilterCondition.IS) {
            if (filter.fieldType() == FilterFieldType.NUMBER) {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(new java.math.BigDecimal(value));

                return """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND CAST(lcfv.value AS NUMERIC) = ?
                    )
                    """;
            }

            if (filter.fieldType() == FilterFieldType.DATE) {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(LocalDate.parse(value));

                return """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND CAST(lcfv.value AS DATE) = ?
                    )
                    """;
            }

            if (filter.fieldType() == FilterFieldType.BOOLEAN) {
                parameters.add(UUID.fromString(fieldId));
                parameters.add(value.toLowerCase(java.util.Locale.ROOT));

                return """
                    EXISTS (
                        SELECT 1
                        FROM lead_custom_field_values lcfv
                        WHERE lcfv.lead_id = leads.id
                          AND lcfv.field_id = ?
                          AND LOWER(lcfv.value) = LOWER(?)
                    )
                    """;
            }
        }

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