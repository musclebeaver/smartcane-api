package com.smartcane.api.domain.billing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VirtualAccountInfo(
        String accountNumber,
        String bank,           // 은행 코드
        String customerName,
        OffsetDateTime dueDate // 입금 기한
) {}
