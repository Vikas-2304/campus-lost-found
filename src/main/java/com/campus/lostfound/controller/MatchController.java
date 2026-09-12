package com.campus.lostfound.controller;

import com.campus.lostfound.dto.MatchResponse;
import com.campus.lostfound.service.MatchingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "View possible matches for your items")
public class MatchController {

    private final MatchingService matchingService;

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<MatchResponse>> getMatches(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(matchingService.getMatchesForItem(id, userDetails.getUsername()));
    }
}