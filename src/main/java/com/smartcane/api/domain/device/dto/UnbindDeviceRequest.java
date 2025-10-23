package com.smartcane.api.domain.device.dto;

import jakarta.validation.constraints.NotNull;
public record UnbindDeviceRequest(
        @NotNull Long userId   // 내부 Long ID 기반으로 요청 처리
) {}
