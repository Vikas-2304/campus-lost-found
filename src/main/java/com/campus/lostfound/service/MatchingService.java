package com.campus.lostfound.service;

import com.campus.lostfound.dto.MatchResponse;
import com.campus.lostfound.entity.*;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ItemRepository itemRepository;
    private final MatchRepository matchRepository;

    /**
     * Triggered automatically when a new item is posted.
     * Checks it against all open opposite-type items.
     */
    @Transactional
    public void findMatchesForNewItem(Item newItem) {
        ItemType oppositeType = (newItem.getType() == ItemType.LOST) ? ItemType.FOUND : ItemType.LOST;

        // Candidate pool: only open or matched items of the opposite type
        List<Item> candidates = itemRepository.findByTypeAndStatusIn(
                oppositeType,
                Arrays.asList(ItemStatus.OPEN, ItemStatus.MATCHED)
        );

        for (Item candidate : candidates) {
            // RULE 1: Never match against the same user's own items
            if (newItem.getPostedBy().getId().equals(candidate.getPostedBy().getId())) {
                continue;
            }

            // Calculate the score
            int score = calculateScore(newItem, candidate);

            // THRESHOLD: If score >= 50, it's a match!
            if (score >= 50) {
                // Identify which is lost and which is found for the DB constraint
                Item lostItem = (newItem.getType() == ItemType.LOST) ? newItem : candidate;
                Item foundItem = (newItem.getType() == ItemType.FOUND) ? newItem : candidate;

                // RULE 2: Prevent duplicate match rows
                if (!matchRepository.existsByLostItemAndFoundItem(lostItem, foundItem)) {
                    Match match = Match.builder()
                            .lostItem(lostItem)
                            .foundItem(foundItem)
                            .score(score)
                            .build();
                    matchRepository.save(match);

                    // Optional: update item statuses to MATCHED so they stand out
                    if (newItem.getStatus() == ItemStatus.OPEN) newItem.setStatus(ItemStatus.MATCHED);
                    if (candidate.getStatus() == ItemStatus.OPEN) candidate.setStatus(ItemStatus.MATCHED);
                    itemRepository.save(newItem);
                    itemRepository.save(candidate);
                }
            }
        }
    }

    /**
     * Fetches active matches for a specific item. Owner only.
     */
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatchesForItem(Long itemId, String userEmail) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        // Ownership check
        if (!item.getPostedBy().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You can only view matches for your own items");
        }

        List<Match> matches = matchRepository.findByLostItemOrFoundItem(item, item);

        return matches.stream()
                // RULE 3: Don't show matches if the other item is CLOSED or CLAIMED
                .filter(m -> {
                    Item other = m.getLostItem().getId().equals(itemId) ? m.getFoundItem() : m.getLostItem();
                    return other.getStatus() != ItemStatus.CLOSED && other.getStatus() != ItemStatus.CLAIMED;
                })
                .map(m -> {
                    Item other = m.getLostItem().getId().equals(itemId) ? m.getFoundItem() : m.getLostItem();
                    return new MatchResponse(
                            m.getId(),
                            other.getId(),
                            other.getTitle(),
                            other.getType(),
                            other.getStatus(),
                            m.getScore(),
                            m.getCreatedAt()
                    );
                })
                .sorted(Comparator.comparingInt(MatchResponse::score).reversed()) // Highest score first
                .toList();
    }

    // --- SCORING ALGORITHM (Explain this in interviews!) ---

    private int calculateScore(Item item1, Item item2) {
        int score = 0;

        // 1. Category Match: +40 points
        if (item1.getCategory().equalsIgnoreCase(item2.getCategory())) {
            score += 40;
        }

        // 2. Location Word Overlap: +20 points
        if (hasWordOverlap(item1.getLocation(), item2.getLocation())) {
            score += 20;
        }

        // 3. Description Keyword Overlap (Jaccard Similarity): +30 points
        if (calculateJaccardSimilarity(item1.getDescription(), item2.getDescription()) >= 0.15) {
            score += 30;
        }

        // 4. Date Proximity (within 3 days): +10 points
        long daysBetween = Math.abs(ChronoUnit.DAYS.between(item1.getEventDate(), item2.getEventDate()));
        if (daysBetween <= 3) {
            score += 10;
        }

        return score;
    }

    private boolean hasWordOverlap(String text1, String text2) {
        Set<String> words1 = tokenize(text1);
        Set<String> words2 = tokenize(text2);
        words1.retainAll(words2); // intersection
        return !words1.isEmpty();
    }

    private double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> set1 = tokenize(text1);
        Set<String> set2 = tokenize(text2);
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        // Split by non-word characters, lowercase, and ignore tiny words like "a", "the", "is"
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 3)
                .collect(Collectors.toSet());
    }
}