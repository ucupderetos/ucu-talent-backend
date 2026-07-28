package ucu.retojulio2026.talent.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ucu.retojulio2026.talent.storage.dto.StorageUploadResponse;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png"
    );

    private final Storage storage;
    private final String bucketName;
    private final String publicBaseUrl;

    public StorageServiceImpl(
            Storage storage,
            @Value("${gcs.bucket-name}") String bucketName,
            @Value("${app.storage.public-base-url:https://storage.googleapis.com}") String publicBaseUrl
    ) {
        this.storage = storage;
        this.bucketName = bucketName;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public StorageUploadResponse uploadImage(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectName = "images/" + UUID.randomUUID() + "-" + safeFilename;

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        try {
            storage.create(blobInfo, file.getBytes());
        } catch (StorageException e) {
            throw mapGcsException(e);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se pudo leer el archivo enviado.",
                    e
            );
        }

        String gcsUri = "gs://" + bucketName + "/" + objectName;
        String publicUrl = publicBaseUrl + "/" + bucketName + "/" + objectName;

        return new StorageUploadResponse(
                objectName,
                originalFilename,
                file.getContentType(),
                file.getSize(),
                bucketName,
                gcsUri,
                publicUrl
        );
    }

    @Override
    public void delete(String objectName) {
        try {
            boolean deleted = storage.delete(BlobId.of(bucketName, objectName));
            if (!deleted) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el archivo para eliminar."
                );
            }
        } catch (StorageException e) {
            throw mapGcsException(e);
        }
    }

    private ResponseStatusException mapGcsException(StorageException e) {
        int code = e.getCode();

        if (code == 403) {
            return new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permisos para subir archivos a este bucket.",
                    e
            );
        }

        if (code == 404) {
            return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El bucket o el objeto no existe.",
                    e
            );
        }

        if (code == 409) {
            return new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conflicto al subir el archivo.",
                    e
            );
        }

        return new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo subir el archivo al almacenamiento.",
                e
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo es obligatorio."
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten imágenes JPG y PNG."
            );
        }
    }
}