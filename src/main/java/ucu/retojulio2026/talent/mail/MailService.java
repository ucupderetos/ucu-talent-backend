package ucu.retojulio2026.talent.mail;

public interface MailService {

    void sendCompanyNewApplicationEmail(String companyEmail, String applicantName, String vacancyName);

    void sendApplicationVistoEmail(String applicantEmail, String studentName, String vacancyName);

    void sendVacancyClosedEmail(String applicantEmail, String studentName, String vacancyName);

    void sendVacancySelectedEmail(String applicantEmail, String studentName, String vacancyName, String companyName);
}
