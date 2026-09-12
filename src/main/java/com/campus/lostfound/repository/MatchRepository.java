package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    // Prevents creating duplicate matches for the same pair.
    boolean existsByLostItemAndFoundItem(Item lostItem, Item foundItem);

    // All matches involving this item (whether it's the lost or found side).
    List<Match> findByLostItemOrFoundItem(Item lostItem, Item foundItem);
}