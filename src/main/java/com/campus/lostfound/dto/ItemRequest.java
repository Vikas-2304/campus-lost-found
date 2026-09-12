package com.campus.lostfound.dto;

import com.campus.lostfound.entity.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ItemRequest(
        @NotNull(message = "Type is required") ItemType type,
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Description is required") String description,
        @NotBlank(message = "Category is required") String category,
        @NotBlank(message = "Location is required") String location,
        String imageUrl, // Optional
        @NotNull(message = "Event date is required") LocalDate eventDate,
        String verificationQuestion // Optional, only useful for FOUND items
) {}