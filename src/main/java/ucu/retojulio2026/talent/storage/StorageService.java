package ucu.retojulio2026.talent.storage;

import org.springframework.web.multipart.MultipartFile;
import ucu.retojulio2026.talent.storage.dto.StorageUploadResponse;

public interface StorageService {
    public StorageUploadResponse uploadImage(MultipartFile file);

    void delete(String objectName);

    //byte[] download(String objectName);
}
