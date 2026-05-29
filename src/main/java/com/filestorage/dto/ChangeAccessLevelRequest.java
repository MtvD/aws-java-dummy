package com.filestorage.dto;

import com.filestorage.model.AccessLevel;
import jakarta.validation.constraints.NotNull;

public record ChangeAccessLevelRequest(
        @NotNull AccessLevel accessLevel
) {}
