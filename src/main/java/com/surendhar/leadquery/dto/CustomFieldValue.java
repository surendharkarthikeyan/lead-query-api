package com.surendhar.leadquery.dto;

import java.util.UUID;

public record CustomFieldValue(
        UUID fieldId,
        String label,
        String value
) {
}
