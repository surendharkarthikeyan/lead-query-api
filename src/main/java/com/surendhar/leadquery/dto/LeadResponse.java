package com.surendhar.leadquery.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        String name,
        String phone,
        String countryCode,
        String e164,
        String email,
        UUID assignedTo,
        LocalDate followUpDate,
        String status,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}