package com.campus.lostfound.dto;

import jakarta.validation.constraints.NotBlank;

public record ClaimRequest(
        @NotBlank(message = "Verification answer is required") String verificationAnswer
) {}