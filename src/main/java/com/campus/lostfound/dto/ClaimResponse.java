package com.campus.lostfound.dto;

import com.campus.lostfound.entity.ClaimStatus;
import java.time.LocalDateTime;

public record ClaimResponse(
        Long id,
        Long claimantId,
        String claimantName,
        String verificationAnswer, // NEVER exposed publicly, only to the item owner
        ClaimStatus status,
        LocalDateTime createdAt
) {}