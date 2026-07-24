package ucu.retojulio2026.talent.mail;

import java.util.List;
import java.util.Map;

import ucu.retojulio2026.talent.mail.dto.UpdateMailTemplateRequest;

public interface MailTemplateService {

    List<MailTemplate> getAll();

    MailTemplate getByCode(MailTemplateCode code);

    MailTemplate updateByCode(MailTemplateCode code, UpdateMailTemplateRequest request);

    RenderedMail render(MailTemplateCode code, Map<String, String> variables);

    record RenderedMail(String subject, String body) {
    }
}
