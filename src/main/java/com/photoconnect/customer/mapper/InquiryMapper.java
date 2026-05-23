package com.photoconnect.customer.mapper;

import com.photoconnect.customer.domain.Inquiry;
import com.photoconnect.customer.dto.InquiryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InquiryMapper {
    InquiryResponse toResponse(Inquiry inquiry);
}
