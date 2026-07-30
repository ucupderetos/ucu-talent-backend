package ucu.retojulio2026.talent.vacancyapplication.dto;

import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;

public record ApplicationListItemRow(
        VacancyApplication application,
        String studentName,
        String studentSurname,
        String studentEmail,
        String vacancyId,
        String vacancyName,
        String companyId,
        String companyName
) {}
