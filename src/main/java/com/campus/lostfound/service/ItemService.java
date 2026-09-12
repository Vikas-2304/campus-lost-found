package com.campus.lostfound.service;

import com.campus.lostfound.dto.ItemRequest;
import com.campus.lostfound.dto.ItemResponse;
import com.campus.lostfound.entity.*;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final MatchingService matchingService;

    @Transactional
    public ItemResponse createItem(ItemRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Item item = Item.builder()
                .postedBy(user)
                .type(request.type())
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .location(request.location())
                .imageUrl(request.imageUrl())
                .eventDate(request.eventDate())
                .verificationQuestion(request.verificationQuestion())
                .status(ItemStatus.OPEN)
                .build();

        Item savedItem = itemRepository.save(item);

        // 🔥 TRIGGER THE MATCHING ENGINE
        matchingService.findMatchesForNewItem(savedItem);

        return mapToResponse(savedItem, user);
    }
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id, String userEmail) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        User currentUser = (userEmail != null) ? userRepository.findByEmail(userEmail).orElse(null) : null;
        return mapToResponse(item, currentUser);
    }
    @Transactional(readOnly = true)
    public Page<ItemResponse> browseItems(ItemType type, String category, String location,
                                          LocalDate from, LocalDate to, Pageable pageable, String userEmail) {
        User currentUser = (userEmail != null) ? userRepository.findByEmail(userEmail).orElse(null) : null;

        // Dynamic filtering using JPA Specifications
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Public browsing only shows OPEN or MATCHED items
            predicates.add(root.get("status").in(ItemStatus.OPEN, ItemStatus.MATCHED));

            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (category != null && !category.isBlank()) predicates.add(cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%"));
            if (location != null && !location.isBlank()) predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), to));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return itemRepository.findAll(spec, pageable).map(item -> mapToResponse(item, currentUser));
    }
    @Transactional(readOnly = true)
    public List<ItemResponse> getMyItems(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return itemRepository.findByPostedByOrderByIdDesc(user)
                .stream()
                .map(item -> mapToResponse(item, user))
                .toList();
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request, String userEmail) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        checkOwnership(item, userEmail);

        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setCategory(request.category());
        item.setLocation(request.location());
        item.setImageUrl(request.imageUrl());
        item.setEventDate(request.eventDate());
        item.setVerificationQuestion(request.verificationQuestion());

        return mapToResponse(itemRepository.save(item), item.getPostedBy());
    }

    @Transactional
    public void closeItem(Long id, String userEmail) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        checkOwnership(item, userEmail);
        item.setStatus(ItemStatus.CLOSED);
        itemRepository.save(item);
    }

    // --- Helper Methods ---

    private void checkOwnership(Item item, String userEmail) {
        if (!item.getPostedBy().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You are not the owner of this item");
        }
    }

    private ItemResponse mapToResponse(Item item, User currentUser) {
        boolean isOwner = currentUser != null
                && currentUser.getId().equals(item.getPostedBy().getId());

        // Security: hide the private question from everyone except the owner
        String question = isOwner ? item.getVerificationQuestion() : null;

        return new ItemResponse(
                item.getId(),
                item.getPostedBy().getId(),
                item.getPostedBy().getName(),
                item.getType(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getLocation(),
                item.getImageUrl(),
                item.getStatus(),
                item.getEventDate(),
                item.getCreatedAt(),
                question,
                isOwner
        );
    }
}