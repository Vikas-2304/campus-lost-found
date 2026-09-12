package com.campus.lostfound.service;

import com.campus.lostfound.entity.*;
import com.campus.lostfound.repository.ClaimRepository;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ClaimService claimService;

    private User owner;
    private User claimant1;
    private User claimant2;
    private Item foundItem;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("owner@college.edu").build();
        claimant1 = User.builder().id(2L).email("c1@college.edu").build();
        claimant2 = User.builder().id(3L).email("c2@college.edu").build();

        foundItem = Item.builder()
                .id(10L).postedBy(owner).type(ItemType.FOUND)
                .status(ItemStatus.OPEN).verificationQuestion("What color is it?")
                .build();
    }

    @Test
    void shouldPreventClaimingOwnItem() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(foundItem));
        when(userRepository.findByEmail("owner@college.edu")).thenReturn(Optional.of(owner));

        assertThrows(IllegalArgumentException.class, () ->
                claimService.createClaim(10L, new com.campus.lostfound.dto.ClaimRequest("Blue"), "owner@college.edu")
        );
    }

    @Test
    void shouldAutoRejectOtherPendingClaimsWhenOneIsApproved() {
        // Setup: Claim 1 and Claim 2 are both PENDING
        Claim claim1 = Claim.builder().id(1L).item(foundItem).claimant(claimant1).status(ClaimStatus.PENDING).build();
        Claim claim2 = Claim.builder().id(2L).item(foundItem).claimant(claimant2).status(ClaimStatus.PENDING).build();

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim1));

        // DELETED: when(itemRepository.findById...) because approveClaim doesn't fetch the item from the DB!

        when(itemRepository.save(any(Item.class))).thenReturn(foundItem);
        when(claimRepository.findByItemAndStatus(foundItem, ClaimStatus.PENDING)).thenReturn(List.of(claim1, claim2));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArguments()[0]);

        // Action: Owner approves Claim 1
        claimService.approveClaim(1L, "owner@college.edu");

        // Assertions: Claim 1 is APPROVED, Item is CLAIMED, Claim 2 is REJECTED
        assertEquals(ClaimStatus.APPROVED, claim1.getStatus());
        assertEquals(ItemStatus.CLAIMED, foundItem.getStatus());
        assertEquals(ClaimStatus.REJECTED, claim2.getStatus());

        // Verify save was called for the item and both claims
        verify(itemRepository, times(1)).save(foundItem);
        verify(claimRepository, atLeast(2)).save(any(Claim.class));
    }
}