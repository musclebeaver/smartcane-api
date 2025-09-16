package com.smartcane.api.domain.device.dto;


import jakarta.validation.constraints.*;


public record DeviceRegisterRequest(
        @NotBlank String serialNo,
        @Size(max = 128) String displayName
){}