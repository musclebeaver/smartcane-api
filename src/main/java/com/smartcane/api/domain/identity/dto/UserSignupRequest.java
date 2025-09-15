package com.smartcane.api.domain.identity.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UserSignupRequest(
        @Email @NotBlank String email,
        @Size(min = 2, max = 60) String nickname,
        LocalDate birthDate,
        @NotBlank @Size(min = 8, max = 100) String password
) {}
