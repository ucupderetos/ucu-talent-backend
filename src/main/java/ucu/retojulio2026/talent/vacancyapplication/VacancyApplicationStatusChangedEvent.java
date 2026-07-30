package ucu.retojulio2026.talent.vacancyapplication;

public record VacancyApplicationStatusChangedEvent(
        String vacancyId,
        String studentProfileId,
        VacancyApplicationStatus newStatus
) {}
