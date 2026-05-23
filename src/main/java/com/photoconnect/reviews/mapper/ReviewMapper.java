package com.photoconnect.reviews.mapper;

import com.photoconnect.reviews.domain.Review;
import com.photoconnect.reviews.dto.ReviewResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewResponse toResponse(Review review);
}
