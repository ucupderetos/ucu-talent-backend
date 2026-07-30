package ucu.retojulio2026.talent.vacancy.dto;

import ucu.retojulio2026.talent.vacancy.Vacancy;

public record VacancyManagementRow(
        Vacancy vacancy,
        String companyName,
        String areaName,
        long applicationCount,
        long newApplicationsCount
) {}
