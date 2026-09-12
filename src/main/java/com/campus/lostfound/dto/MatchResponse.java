package com.campus.lostfound.dto;

import com.campus.lostfound.entity.ItemStatus;
import com.campus.lostfound.entity.ItemType;
import java.time.LocalDateTime;

public record MatchResponse(
        Long matchId,
        Long otherItemId,
        String otherItemTitle,
        ItemType otherItemType,
        ItemStatus otherItemStatus,
        int score,
        LocalDateTime createdAt
) {}