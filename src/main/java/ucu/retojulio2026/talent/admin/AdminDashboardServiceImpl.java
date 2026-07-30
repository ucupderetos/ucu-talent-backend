package ucu.retojulio2026.talent.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ucu.retojulio2026.talent.admin.dto.AdminDashboardResponse;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.company.dto.PendingCompanyRow;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.VacancyService;
import ucu.retojulio2026.talent.vacancy.dto.RecentVacancyRow;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int RECENT_VACANCIES_LIMIT = 5;
    private static final int PENDING_COMPANIES_LIMIT = 10;

    private final CompanyService companyService;
    private final VacancyService vacancyService;
    private final VacancyApplicationService vacancyApplicationService;
    private final UserService userService;

    public AdminDashboardServiceImpl(CompanyService companyService,
                                     VacancyService vacancyService,
                                     VacancyApplicationService vacancyApplicationService,
                                     UserService userService) {
        this.companyService = companyService;
        this.vacancyService = vacancyService;
        this.vacancyApplicationService = vacancyApplicationService;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        Map<VacancyApplicationStatus, Long> applicationsByStatus = vacancyApplicationService.countByStatusSummary();

        AdminDashboardResponse.Counts counts = new AdminDashboardResponse.Counts(
                new AdminDashboardResponse.CompanyCounts(
                        companyService.count(),
                        companyService.countByAccountStatus(AccountStatus.PENDIENTE)),
                new AdminDashboardResponse.VacancyCounts(
                        vacancyService.countNotDeleted(),
                        vacancyService.countPublished()),
                new AdminDashboardResponse.ApplicationCounts(
                        vacancyApplicationService.count(),
                        applicationsByStatus.getOrDefault(VacancyApplicationStatus.PENDIENTE, 0L)),
                new AdminDashboardResponse.UserCounts(
                        userService.count(),
                        userService.countByRole(Role.ALUMNO),
                        userService.countByRole(Role.EMPRESA),
                        userService.countByRole(Role.ADMIN)));

        List<AdminDashboardResponse.ApplicationStatusCount> applicationStatusSummary =
                java.util.Arrays.stream(VacancyApplicationStatus.values())
                        .map(status -> new AdminDashboardResponse.ApplicationStatusCount(
                                status, applicationsByStatus.getOrDefault(status, 0L)))
                        .toList();

        List<AdminDashboardResponse.RecentVacancy> recentVacancies =
                vacancyService.getRecentForDashboard(RECENT_VACANCIES_LIMIT)
                        .stream()
                        .map(this::toRecentVacancy)
                        .toList();

        List<AdminDashboardResponse.PendingCompany> pendingCompanies =
                companyService.getPendingForDashboard(PENDING_COMPANIES_LIMIT)
                        .stream()
                        .map(this::toPendingCompany)
                        .toList();

        return new AdminDashboardResponse(counts, applicationStatusSummary, recentVacancies, pendingCompanies);
    }

    private AdminDashboardResponse.RecentVacancy toRecentVacancy(RecentVacancyRow row) {
        return new AdminDashboardResponse.RecentVacancy(
                row.vacancyId(), row.name(), row.companyName(),
                row.publicationDate(), row.status(), row.applicationCount());
    }

    private AdminDashboardResponse.PendingCompany toPendingCompany(PendingCompanyRow row) {
        return new AdminDashboardResponse.PendingCompany(
                row.companyId(), row.name(), row.industry(), row.registeredAt());
    }
}
