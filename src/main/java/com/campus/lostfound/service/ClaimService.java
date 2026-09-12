package com.campus.lostfound.service;

import com.campus.lostfound.dto.ClaimRequest;
import com.campus.lostfound.dto.ClaimResponse;
import com.campus.lostfound.entity.*;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.ClaimRepository;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClaimResponse createClaim(Long itemId, ClaimRequest request, String userEmail) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        User claimant = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // RULE 1: Can only claim FOUND items (you don't "claim" a lost item, you post a found one)
        if (item.getType() != ItemType.FOUND) {
            throw new IllegalArgumentException("You can only claim FOUND items.");
        }

        // RULE 2: Can't claim your own item
        if (item.getPostedBy().getId().equals(claimant.getId())) {
            throw new IllegalArgumentException("You cannot claim your own item.");
        }

        // RULE 3: Item must be OPEN or MATCHED to accept claims
        if (item.getStatus() != ItemStatus.OPEN && item.getStatus() != ItemStatus.MATCHED) {
            throw new IllegalArgumentException("This item is no longer accepting claims.");
        }

        Claim claim = Claim.builder()
                .item(item)
                .claimant(claimant)
                .verificationAnswer(request.verificationAnswer())
                .status(ClaimStatus.PENDING)
                .build();

        return mapToResponse(claimRepository.save(claim));
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsForItem(Long itemId, String userEmail) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        // Ownership check: ONLY the owner can see claims and verification answers
        if (!item.getPostedBy().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Only the item owner can view claims.");
        }

        return claimRepository.findByItem(item).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ClaimResponse approveClaim(Long claimId, String userEmail) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        checkItemOwnership(claim.getItem(), userEmail);
        ensureClaimIsPending(claim);

        // 1. Approve this claim
        claim.setStatus(ClaimStatus.APPROVED);
        claimRepository.save(claim);

        // 2. Lock the item
        Item item = claim.getItem();
        item.setStatus(ItemStatus.CLAIMED);
        itemRepository.save(item);

        // 3. AUTO-REJECT all other pending claims for this item!
        List<Claim> otherPendingClaims = claimRepository.findByItemAndStatus(item, ClaimStatus.PENDING);
        for (Claim other : otherPendingClaims) {
            if (!other.getId().equals(claimId)) {
                other.setStatus(ClaimStatus.REJECTED);
                claimRepository.save(other);
            }
        }

        return mapToResponse(claim);
    }

    @Transactional
    public ClaimResponse rejectClaim(Long claimId, String userEmail) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        checkItemOwnership(claim.getItem(), userEmail);
        ensureClaimIsPending(claim);

        claim.setStatus(ClaimStatus.REJECTED);
        return mapToResponse(claimRepository.save(claim));
    }

    // --- Helpers ---

    private void checkItemOwnership(Item item, String userEmail) {
        if (!item.getPostedBy().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Only the item owner can manage claims.");
        }
    }

    private void ensureClaimIsPending(Claim claim) {
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("This claim has already been " + claim.getStatus().name().toLowerCase() + ".");
        }
    }

    private ClaimResponse mapToResponse(Claim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getClaimant().getId(),
                claim.getClaimant().getName(),
                claim.getVerificationAnswer(),
                claim.getStatus(),
                claim.getCreatedAt()
        );
    }
}