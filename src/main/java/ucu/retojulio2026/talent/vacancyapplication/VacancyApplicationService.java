package ucu.retojulio2026.talent.vacancyapplication;

import ucu.retojulio2026.talent.vacancyapplication.dto.ApplicationListItemResponse;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.MyApplicationRowResponse;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationStudentResponse;

import java.util.List;
import java.util.Map;

public interface VacancyApplicationService {

    VacancyApplication create(CreateVacancyApplicationRequest vacancyApplication);

    Map<VacancyApplicationStatus, Long> countByStatusSummary();

    VacancyApplication getById(String id);

    List<VacancyApplication> getAll();

    List<VacancyApplication> getByVacancyId(String vacancyId);

    List<VacancyApplication> getByStudentProfileId(String studentProfileId);

    List<VacancyApplicationStudentResponse> getStudentApplications(String studentProfileId);

    List<MyApplicationRowResponse> getMyApplicationsDetailed(String studentProfileId);

    List<ApplicationListItemResponse> getDetailedByVacancyId(String vacancyId);

    List<ApplicationListItemResponse> getAllDetailed();

    List<VacancyApplication> getByStatus(VacancyApplicationStatus status);

    VacancyApplication update(String id, VacancyApplicationStatus status);

    VacancyApplication accept(String id);

    void delete(String id);

    long count();
}
