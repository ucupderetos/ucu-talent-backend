package ucu.retojulio2026.talent.mail.dto;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.mail.MailTemplate;
import ucu.retojulio2026.talent.mail.MailTemplateCode;

@Component
public class MailTemplateMapper {

    private static final Map<MailTemplateCode, List<String>> PLACEHOLDERS_BY_CODE = Map.of(
            MailTemplateCode.NEW_APPLICATION, List.of("applicantName", "vacancyName"),
            MailTemplateCode.APPLICATION_VISTO, List.of("studentName", "vacancyName"),
            MailTemplateCode.VACANCY_CLOSED, List.of("studentName", "vacancyName"),
            MailTemplateCode.VACANCY_SELECTED, List.of("studentName", "vacancyName", "companyName"));

    public MailTemplateResponse toResponse(MailTemplate mailTemplate) {
        List<String> placeholders = PLACEHOLDERS_BY_CODE.getOrDefault(mailTemplate.getCode(), List.of());
        return new MailTemplateResponse(
                mailTemplate.getMailTemplateId(),
                mailTemplate.getCode(),
                mailTemplate.getSubject(),
                mailTemplate.getBody(),
                placeholders);
    }
}
