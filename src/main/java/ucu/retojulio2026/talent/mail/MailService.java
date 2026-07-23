package ucu.retojulio2026.talent.mail;

import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

public interface MailService {

    void sendCompanyNewApplicationEmail(String companyEmail, String applicantName, String vacancyName);

    void sendApplicantStatusChangedEmail(String applicantEmail, String vacancyName, VacancyApplicationStatus newStatus);
}
