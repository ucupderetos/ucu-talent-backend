package ucu.retojulio2026.talent.storage.dto;

public record StorageUploadResponse(
        String objectName,
        String originalFilename,
        String contentType,
        long size,
        String bucket,
        String gcsUri,
        String publicUrl
) {}