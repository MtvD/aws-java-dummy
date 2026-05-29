package com.filestorage.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String token,
        LocalDateTime expiresAt
) {}
