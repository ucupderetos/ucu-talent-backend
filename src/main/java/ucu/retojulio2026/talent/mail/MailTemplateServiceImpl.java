package ucu.retojulio2026.talent.mail;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.mail.dto.UpdateMailTemplateRequest;

@Service
public class MailTemplateServiceImpl implements MailTemplateService {

    private final MailTemplateRepository mailTemplateRepository;

    public MailTemplateServiceImpl(MailTemplateRepository mailTemplateRepository) {
        this.mailTemplateRepository = mailTemplateRepository;
    }

    @Override
    public List<MailTemplate> getAll() {
        return mailTemplateRepository.findAll();
    }

    @Override
    public MailTemplate getByCode(MailTemplateCode code) {
        return mailTemplateRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MailTemplate con code '" + code + "' no encontrado"));
    }

    @Override
    public MailTemplate updateByCode(MailTemplateCode code, UpdateMailTemplateRequest request) {
        MailTemplate mailTemplate = getByCode(code);
        mailTemplate.setSubject(request.subject());
        mailTemplate.setBody(request.body());
        return mailTemplateRepository.save(mailTemplate);
    }

    @Override
    public RenderedMail render(MailTemplateCode code, Map<String, String> variables) {
        MailTemplate mailTemplate = getByCode(code);
        String subject = MailTemplateRenderer.render(mailTemplate.getSubject(), variables);
        String body = MailTemplateRenderer.render(mailTemplate.getBody(), variables);
        return new RenderedMail(subject, body);
    }
}
