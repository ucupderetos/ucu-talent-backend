package ucu.retojulio2026.talent.storage;

import org.springframework.web.multipart.MultipartFile;
import ucu.retojulio2026.talent.storage.dto.StorageUploadResponse;

import java.net.URL;
import java.time.Duration;

public interface StorageService {
    StorageUploadResponse upload(MultipartFile file, String folder);

    URL getSignedUrl(String objectName, Duration ttl);

    void delete(String objectName);

}
