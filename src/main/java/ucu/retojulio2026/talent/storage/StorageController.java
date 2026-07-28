package ucu.retojulio2026.talent.storage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ucu.retojulio2026.talent.storage.dto.StorageUploadResponse;

@RestController
@RequestMapping("/storage")
@Tag(name = "Storage", description = "Subida y eliminación de archivos en Google Cloud Storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Operation(summary = "Subir una imagen")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StorageUploadResponse> uploadImage(@RequestPart("file") MultipartFile file) {
        StorageUploadResponse response = storageService.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Eliminar una imagen")
    @DeleteMapping("/images")
    public ResponseEntity<Void> deleteImage(@RequestParam String objectName) {
        storageService.delete(objectName);
        return ResponseEntity.noContent().build();
    }
}