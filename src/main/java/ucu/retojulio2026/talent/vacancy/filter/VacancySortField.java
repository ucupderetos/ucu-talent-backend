package ucu.retojulio2026.talent.vacancy.filter;

public enum VacancySortField {
    PUBLICATION_DATE("publicationDate"),
    CLOSING_DATE("closingDate");

    private final String propertyName;

    VacancySortField(String propertyName) {
        this.propertyName = propertyName;
    }

    public String propertyName() {
        return propertyName;
    }
}
