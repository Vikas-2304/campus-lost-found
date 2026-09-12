package com.campus.lostfound.dto;

import com.campus.lostfound.entity.ItemStatus;
import com.campus.lostfound.entity.ItemType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        Long postedById,
        String postedByName,
        ItemType type,
        String title,
        String description,
        String category,
        String location,
        String imageUrl,
        ItemStatus status,
        LocalDate eventDate,
        LocalDateTime createdAt,
        String verificationQuestion, // ONLY populated for the owner
        boolean isOwner              // computed server-side per requesting user
) {}