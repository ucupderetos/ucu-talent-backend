package ucu.retojulio2026.talent.vacancyapplication.dto;

import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

import java.time.LocalDate;

public record VacancyApplicationResponse(
        String id,
        String vacancyId,
        String studentProfileId,
        VacancyApplicationStatus status,
        LocalDate appliedAt
) {}
