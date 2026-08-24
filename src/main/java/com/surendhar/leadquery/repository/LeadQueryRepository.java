package com.surendhar.leadquery.repository;

import com.surendhar.leadquery.dto.LeadFilter;
import com.surendhar.leadquery.dto.LeadResponse;
import com.surendhar.leadquery.util.SystemFieldMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class LeadQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public LeadQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LeadResponse> findLeadsByTenant(
            UUID tenantId,
            int limit,
            int offset,
            String sortBy,
            String sortDirection,
            String q,
            List<LeadFilter> filters,
            String logic
    ) {

        /*
         * ---------------------------------------------------------
         * 1. Map API sort fields to database columns
         * ---------------------------------------------------------
         */

        String orderByColumn = switch (sortBy) {
            case "createdAt" -> "created_at";
            case "followUpDate" -> "follow_up_date";
            default -> throw new IllegalArgumentException(
                    "Invalid sort field"
            );
        };

        String direction = switch (sortDirection.toLowerCase()) {
            case "asc" -> "ASC";
            case "desc" -> "DESC";
            default -> throw new IllegalArgumentException(
                    "Invalid sort direction"
            );
        };


        /*
         * ---------------------------------------------------------
         * 2. Base SQL
         * ---------------------------------------------------------
         */

        StringBuilder sql = new StringBuilder("""
                SELECT
                    id,
                    tenant_id,
                    user_id,
                    name,
                    phone,
                    country_code,
                    e164,
                    email,
                    assigned_to,
                    follow_up_date,
                    status,
                    notes,
                    created_at,
                    updated_at
                FROM leads
                WHERE tenant_id = ?
                """);


        /*
         * ---------------------------------------------------------
         * 3. SQL parameters
         * ---------------------------------------------------------
         */

        List<Object> parameters = new ArrayList<>();

        // First ? -> tenant_id
        parameters.add(tenantId);


        /*
         * ---------------------------------------------------------
         * 4. Free-text search
         * ---------------------------------------------------------
         *
         * q searches:
         *   name
         *   phone
         *   email
         *   e164
         */

        if (q != null && !q.isBlank()) {

            sql.append("""
                    AND (
                        name ILIKE ?
                        OR phone ILIKE ?
                        OR email ILIKE ?
                        OR e164 ILIKE ?
                    )
                    """);

            String searchValue = "%" + q.trim() + "%";

            parameters.add(searchValue);
            parameters.add(searchValue);
            parameters.add(searchValue);
            parameters.add(searchValue);
        }


        /*
         * ---------------------------------------------------------
         * 5. System-field filters
         * ---------------------------------------------------------
         */

        if (filters != null && !filters.isEmpty()) {

            String filterLogic =
                    "OR".equalsIgnoreCase(logic)
                            ? " OR "
                            : " AND ";

            List<String> conditions = new ArrayList<>();

            for (LeadFilter filter : filters) {

                String condition;

                if (SystemFieldMapper.isSystemField(filter.fieldId())) {

                    condition = LeadFilterSqlBuilder.buildSystemFilter(
                            filter,
                            parameters
                    );

                } else {

                    condition = LeadFilterSqlBuilder.buildCustomFilter(
                            filter,
                            parameters
                    );
                }

                if (condition != null) {
                    conditions.add(condition);
                }
            }


            /*
             * Add filter conditions to SQL.
             */

            if (!conditions.isEmpty()) {

                sql.append(" AND (");

                sql.append(
                        String.join(
                                filterLogic,
                                conditions
                        )
                );

                sql.append(")");
            }
        }


        /*
         * ---------------------------------------------------------
         * 6. Sorting
         * ---------------------------------------------------------
         *
         * We cannot use ? for:
         *
         *   column names
         *   ASC / DESC
         *
         * Therefore the values are mapped/validated above.
         */

        sql.append(
                " ORDER BY %s %s "
                        .formatted(
                                orderByColumn,
                                direction
                        )
        );


        /*
         * ---------------------------------------------------------
         * 7. Pagination
         * ---------------------------------------------------------
         */

        sql.append("""
                LIMIT ?
                OFFSET ?
                """);

        parameters.add(limit);
        parameters.add(offset);


        /*
         * ---------------------------------------------------------
         * 8. Execute query
         * ---------------------------------------------------------
         *
         * JdbcTemplate:
         *
         * 1. Executes SQL
         * 2. Sends parameters to ? placeholders
         * 3. Gets ResultSet
         * 4. RowMapper converts each row to LeadResponse
         */

        return jdbcTemplate.query(
                sql.toString(),

                (rs, rowNum) -> new LeadResponse(

                        rs.getObject(
                                "id",
                                UUID.class
                        ),

                        rs.getObject(
                                "tenant_id",
                                UUID.class
                        ),

                        rs.getObject(
                                "user_id",
                                UUID.class
                        ),

                        rs.getString("name"),

                        rs.getString("phone"),

                        rs.getString("country_code"),

                        rs.getString("e164"),

                        rs.getString("email"),

                        rs.getObject(
                                "assigned_to",
                                UUID.class
                        ),

                        rs.getObject(
                                "follow_up_date",
                                LocalDate.class
                        ),

                        rs.getString("status"),

                        rs.getString("notes"),

                        rs.getObject(
                                "created_at",
                                OffsetDateTime.class
                        ),

                        rs.getObject(
                                "updated_at",
                                OffsetDateTime.class
                        )
                ),

                parameters.toArray()
        );
    }
}