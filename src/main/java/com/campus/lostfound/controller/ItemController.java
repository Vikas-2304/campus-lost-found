package com.campus.lostfound.controller;

import com.campus.lostfound.dto.ItemRequest;
import com.campus.lostfound.dto.ItemResponse;
import com.campus.lostfound.entity.ItemType;
import com.campus.lostfound.service.ItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item management and browsing")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(itemService.createItem(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<Page<ItemResponse>> browseItems(
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return ResponseEntity.ok(itemService.browseItems(type, category, location, from, to, pageable, currentEmail()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ItemResponse>> getMyItems(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(itemService.getMyItems(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id, currentEmail()));
    }


    // Returns the logged-in user's email, or null if anonymous.
    // Reads straight from the SecurityContext — works on public AND protected endpoints.
    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(itemService.updateItem(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        itemService.closeItem(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}