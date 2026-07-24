package ucu.retojulio2026.talent.common;


public final class DocumentNormalizer {

    private DocumentNormalizer() {
    }

    public static String normalize(String documentNumber) {
        if (documentNumber == null) {
            return null;
        }
        return documentNumber.replaceAll("[.\\-\\s]", "");
    }
}
