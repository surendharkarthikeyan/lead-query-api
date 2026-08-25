package com.surendhar.leadquery.util;

import java.util.Map;

public final class SystemFieldMapper {

    private SystemFieldMapper() {
    }

    private static final Map<String, String> FIELD_TO_COLUMN = Map.of(
            "name", "name",
            "phone", "phone",
            "email", "email",
            "e164", "e164",
            "assignedTo", "assigned_to",
            "createdBy", "user_id",
            "followUpDate", "follow_up_date",
            "status", "status",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    public static String getColumn(String fieldId) {
        return FIELD_TO_COLUMN.get(fieldId);
    }

    public static boolean isSystemField(String fieldId) {
        return FIELD_TO_COLUMN.containsKey(fieldId);
    }
}