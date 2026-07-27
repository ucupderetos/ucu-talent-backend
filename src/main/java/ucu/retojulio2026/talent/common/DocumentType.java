package ucu.retojulio2026.talent.common;

import java.util.regex.Pattern;


public enum DocumentType {
    CEDULA_IDENTIDAD(Pattern.compile("^[0-9]+$")),
    DNI(Pattern.compile("^[0-9]+$")),
    PASAPORTE(Pattern.compile("^[A-Za-z0-9]+$"));

    private final Pattern pattern;

    DocumentType(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean isValid(String normalizedDocumentNumber) {
        return normalizedDocumentNumber != null && pattern.matcher(normalizedDocumentNumber).matches();
    }
}
