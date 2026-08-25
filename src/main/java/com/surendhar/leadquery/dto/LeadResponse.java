package com.surendhar.leadquery.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
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
                OffsetDateTime updatedAt,
                List<CustomFieldValue> customFields
) {

        public LeadResponse withCustomFields(List<CustomFieldValue> values) {
                return new LeadResponse(
                                id,
                                tenantId,
                                userId,
                                name,
                                phone,
                                countryCode,
                                e164,
                                email,
                                assignedTo,
                                followUpDate,
                                status,
                                notes,
                                createdAt,
                                updatedAt,
                                values
                );
        }
}