package com.campus.lostfound.dto;

public record AuthResponse(
        String token,
        String message
) {}