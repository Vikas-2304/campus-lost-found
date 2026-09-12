package com.campus.lostfound.controller;

import com.campus.lostfound.dto.ClaimRequest;
import com.campus.lostfound.dto.ClaimResponse;
import com.campus.lostfound.service.ClaimService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Claiming items and owner approvals")
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping("/items/{itemId}/claims")
    public ResponseEntity<ClaimResponse> createClaim(
            @PathVariable Long itemId,
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(claimService.createClaim(itemId, request, userDetails.getUsername()));
    }

    @GetMapping("/items/{itemId}/claims")
    public ResponseEntity<List<ClaimResponse>> getClaims(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(claimService.getClaimsForItem(itemId, userDetails.getUsername()));
    }

    @PutMapping("/claims/{claimId}/approve")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(claimService.approveClaim(claimId, userDetails.getUsername()));
    }

    @PutMapping("/claims/{claimId}/reject")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(claimService.rejectClaim(claimId, userDetails.getUsername()));
    }
}