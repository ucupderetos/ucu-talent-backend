package ucu.retojulio2026.talent.vacancyapplication;

public record VacancyApplicationCreatedEvent(
        String vacancyId,
        String studentProfileId
) {}
