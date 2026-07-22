package ucu.retojulio2026.talent.vacancy.filter;

/**
 * Campos habilitados para ordenar /vacancy/search. Enum para no dejar
 * que el cliente ordene por cualquier columna de la entidad, y para que
 * Swagger documente los valores bien.
 */
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