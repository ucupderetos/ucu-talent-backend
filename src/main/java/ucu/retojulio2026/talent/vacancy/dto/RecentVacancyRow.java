package ucu.retojulio2026.talent.vacancy.dto;

import ucu.retojulio2026.talent.vacancy.VacancyStatus;

import java.time.LocalDate;

public record RecentVacancyRow(
        String vacancyId,
        String name,
        String companyName,
        LocalDate publicationDate,
        VacancyStatus status,
        long applicationCount
) {}
