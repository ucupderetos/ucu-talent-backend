package ucu.retojulio2026.talent.vacancyapplication.dto;

import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;

public record MyApplicationRow(
        VacancyApplication application,
        Vacancy vacancy,
        String companyName,
        String areaName
) {}
