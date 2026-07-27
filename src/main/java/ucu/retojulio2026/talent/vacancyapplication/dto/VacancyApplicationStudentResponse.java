package ucu.retojulio2026.talent.vacancyapplication.dto;

import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

import java.time.LocalDate;

public record VacancyApplicationStudentResponse(
        String vacancyApplicationId,
        String vacancyId,
        String studentProfileId,
        VacancyApplicationStatus status,
        LocalDate appliedAt
) {}
