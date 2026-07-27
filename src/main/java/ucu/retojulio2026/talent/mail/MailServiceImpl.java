package ucu.retojulio2026.talent.mail;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MailServiceImpl implements MailService {
    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final MailValidator mailValidator;
    private final MailTemplateService mailTemplateService;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public MailServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, MailValidator mailValidator,
                            MailTemplateService mailTemplateService) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailValidator = mailValidator;
        this.mailTemplateService = mailTemplateService;
    }

    @Async("taskExecutor")
    @Override
    public void sendCompanyNewApplicationEmail(String companyEmail, String applicantName, String vacancyName) {
        sendTemplated(MailTemplateCode.NEW_APPLICATION, companyEmail, Map.of(
                "applicantName", applicantName,
                "vacancyName", vacancyName));
    }

    @Async("taskExecutor")
    @Override
    public void sendApplicationVistoEmail(String applicantEmail, String studentName, String vacancyName) {
        sendTemplated(MailTemplateCode.APPLICATION_VISTO, applicantEmail, Map.of(
                "studentName", studentName,
                "vacancyName", vacancyName));
    }

    @Async("taskExecutor")
    @Override
    public void sendVacancyClosedEmail(String applicantEmail, String studentName, String vacancyName) {
        sendTemplated(MailTemplateCode.VACANCY_CLOSED, applicantEmail, Map.of(
                "studentName", studentName,
                "vacancyName", vacancyName));
    }

    @Async("taskExecutor")
    @Override
    public void sendVacancySelectedEmail(String applicantEmail, String studentName, String vacancyName, String companyName) {
        sendTemplated(MailTemplateCode.VACANCY_SELECTED, applicantEmail, Map.of(
                "studentName", studentName,
                "vacancyName", vacancyName,
                "companyName", companyName));
    }

    private void sendTemplated(MailTemplateCode code, String to, Map<String, String> variables) {
        if (mailSender == null || mailHost.isBlank()) {
            log.warn("SMTP no configurado: se omite correo '{}' a {}", code, to);
            return;
        }

        String normalizedTo = mailValidator.normalize(to);
        mailValidator.validateOrThrow(normalizedTo);
        try {
            MailTemplateService.RenderedMail renderedMail = mailTemplateService.render(code, variables);
            SimpleMailMessage message = new SimpleMailMessage();
            setFromIfConfigured(message);
            message.setTo(normalizedTo);
            message.setSubject(stripHeaderInjection(renderedMail.subject()));
            message.setText(renderedMail.body());
            mailSender.send(message);
            log.info("Correo '{}' enviado a {}", code, normalizedTo);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo '" + code + "'", e);
        }
    }

    private void setFromIfConfigured(SimpleMailMessage message) {
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
    }

    private static String stripHeaderInjection(String subject) {
        return subject == null ? null : subject.replaceAll("[\\r\\n]", " ");
    }
}
