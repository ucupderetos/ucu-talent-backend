package ucu.retojulio2026.talent.storage.dto;

import org.springframework.core.io.Resource;

public record StorageFileResponse(
        Resource resource,
        String contentType
) {}