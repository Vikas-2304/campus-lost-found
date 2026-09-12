package com.campus.lostfound.service;

import com.campus.lostfound.entity.*;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private MatchingService matchingService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).email("a@college.edu").build();
        user2 = User.builder().id(2L).email("b@college.edu").build();
    }

    @Test
    void shouldCreateMatchWhenScoreIsHigh() {
        Item lostItem = createItem(1L, user1, ItemType.LOST, "Laptop", "Library", "MacBook Pro silver");
        Item foundItem = createItem(2L, user2, ItemType.FOUND, "Laptop", "Library", "Found a silver MacBook");

        when(itemRepository.findByTypeAndStatusIn(any(), any())).thenReturn(List.of(foundItem));
        when(matchRepository.existsByLostItemAndFoundItem(any(), any())).thenReturn(false);

        matchingService.findMatchesForNewItem(lostItem);

        // Score: Category(40) + Location(20) + Jaccard(30) + Date(10) = 100
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    void shouldNotMatchSameUserItems() {
        Item lostItem = createItem(1L, user1, ItemType.LOST, "Laptop", "Library", "MacBook");
        Item foundItem = createItem(2L, user1, ItemType.FOUND, "Laptop", "Library", "MacBook"); // Same user!

        when(itemRepository.findByTypeAndStatusIn(any(), any())).thenReturn(List.of(foundItem));

        matchingService.findMatchesForNewItem(lostItem);

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void shouldNotCreateMatchWhenScoreIsLow() {
        Item lostItem = createItem(1L, user1, ItemType.LOST, "Laptop", "Library", "MacBook Pro");
        Item foundItem = createItem(2L, user2, ItemType.FOUND, "Water Bottle", "Gym", "Blue flask"); // Low score

        when(itemRepository.findByTypeAndStatusIn(any(), any())).thenReturn(List.of(foundItem));

        matchingService.findMatchesForNewItem(lostItem);

        verify(matchRepository, never()).save(any(Match.class));
    }

    // Helper method to quickly build items for tests
    private Item createItem(Long id, User user, ItemType type, String category, String location, String desc) {
        return Item.builder()
                .id(id)
                .postedBy(user)
                .type(type)
                .category(category)
                .location(location)
                .description(desc)
                .eventDate(LocalDate.now())
                .status(ItemStatus.OPEN)
                .build();
    }
}