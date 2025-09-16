package com.smartcane.api.domain.device.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UnbindDeviceRequest(
        @NotNull UUID userId
) {}
