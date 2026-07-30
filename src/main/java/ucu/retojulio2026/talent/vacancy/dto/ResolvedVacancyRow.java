package ucu.retojulio2026.talent.vacancy.dto;

import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.vacancy.Vacancy;

public record ResolvedVacancyRow(
        Vacancy vacancy,
        Company company,
        AccountStatus companyStatus,
        String areaName,
        String parentAreaName
) {}
