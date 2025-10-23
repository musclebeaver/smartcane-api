package com.smartcane.api.domain.device.dto;

import jakarta.validation.constraints.NotNull;

public record BindDeviceRequest(
        @NotNull Long userId   // UUID 대신 내부 Long ID 사용
) {}
