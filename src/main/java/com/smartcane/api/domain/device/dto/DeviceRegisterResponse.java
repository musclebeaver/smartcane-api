package com.smartcane.api.domain.device.dto;


import java.util.UUID;


public record DeviceRegisterResponse(
        UUID deviceId,
        String serialNo,
        String status
){}