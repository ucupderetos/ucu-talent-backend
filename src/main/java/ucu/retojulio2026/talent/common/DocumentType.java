package ucu.retojulio2026.talent.common;

// Tipo de documento compartido por User y UniversityRegistry.
// Se persiste como string (EnumType.STRING); los valores deben coincidir con los CHECK
// de las tablas user (V6) y university_registry (V16).
public enum DocumentType {
    CEDULA_IDENTIDAD,
    PASAPORTE,
    DNI
}
