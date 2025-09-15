package com.smartcane.api.domain.identity.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserUpdateRequest(
        @Size(min = 2, max = 60) String nickname,
        LocalDate birthDate
) {}
