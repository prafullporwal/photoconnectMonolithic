package com.photoconnect.admin.dto;

public record AdminStatsResponse(
        long totalUsers,
        long photographers,
        long customers,
        long totalInquiries,
        long totalPhotographerProfiles
) {}
