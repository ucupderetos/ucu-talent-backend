package ucu.retojulio2026.talent.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

@Service
public class MailServiceImpl implements MailService {
    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final MailValidator mailValidator;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public MailServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, MailValidator mailValidator) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailValidator = mailValidator;
    }

    @Async("taskExecutor")
    @Override
    public void sendCompanyNewApplicationEmail(String companyEmail, String applicantName, String vacancyName) {
        if (mailSender == null) {
            log.warn("SMTP no configurado: se omite correo de nueva postulación a {}", companyEmail);
            return;
        }

        String normalizedCompanyEmail = mailValidator.normalize(companyEmail);
        mailValidator.validateOrThrow(normalizedCompanyEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            setFromIfConfigured(message);
            message.setTo(normalizedCompanyEmail);
            message.setSubject("Nueva postulación recibida");
            message.setText(buildCompanyNewApplicationBody(applicantName, vacancyName));
            mailSender.send(message);
            log.info("Correo de nueva postulación enviado a {}", normalizedCompanyEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo de nueva postulación", e);
        }
    }

    @Async("taskExecutor")
    @Override
    public void sendApplicantStatusChangedEmail(String applicantEmail, String vacancyName, VacancyApplicationStatus newStatus) {
        if (mailSender == null) {
            log.warn("SMTP no configurado: se omite correo de cambio de estado a {}", applicantEmail);
            return;
        }

        String normalizedApplicantEmail = mailValidator.normalize(applicantEmail);
        mailValidator.validateOrThrow(normalizedApplicantEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            setFromIfConfigured(message);
            message.setTo(normalizedApplicantEmail);
            message.setSubject("Tu postulación cambió de estado");
            message.setText(buildApplicantStatusChangedBody(vacancyName, newStatus));
            mailSender.send(message);
            log.info("Correo de cambio de estado enviado a {}", normalizedApplicantEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo de cambio de estado", e);
        }
    }

    private void setFromIfConfigured(SimpleMailMessage message) {
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
    }

    private String buildCompanyNewApplicationBody(String applicantName, String vacancyName) {
        return "Hola,\n\n"
                + "Recibiste una nueva postulación.\n"
                + "Postulante: " + applicantName + "\n"
                + "Puesto: " + vacancyName + "\n\n"
                + "Saludos,\n"
                + "Equipo Talent";
    }

    private String buildApplicantStatusChangedBody(String vacancyName, VacancyApplicationStatus newStatus) {
        return "Hola,\n\n"
                + "Tu postulación cambió de estado.\n"
                + "Puesto: " + vacancyName + "\n"
                + "Nuevo estado: " + newStatus + "\n\n"
                + "Saludos,\n"
                + "Equipo Talent";
    }
}
