package ucu.retojulio2026.talent.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ucu.retojulio2026.talent.common.DocumentBearer;
import ucu.retojulio2026.talent.common.DocumentNormalizer;

public class DocumentNumberValidator implements ConstraintValidator<ValidDocumentNumber, DocumentBearer> {

    @Override
    public boolean isValid(DocumentBearer request, ConstraintValidatorContext context) {
        if (request == null || request.documentType() == null || request.documentNumber() == null) {
            return true;
        }
        String normalized = DocumentNormalizer.normalize(request.documentNumber());
        return request.documentType().isValid(normalized);
    }
}
