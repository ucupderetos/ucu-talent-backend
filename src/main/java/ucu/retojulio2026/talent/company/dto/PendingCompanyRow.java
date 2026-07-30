package ucu.retojulio2026.talent.company.dto;

import java.time.LocalDate;

public record PendingCompanyRow(
        String companyId,
        String name,
        String industry,
        LocalDate registeredAt
) {}
