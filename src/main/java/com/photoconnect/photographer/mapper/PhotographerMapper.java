package com.photoconnect.photographer.mapper;

import com.photoconnect.photographer.domain.PhotographerProfile;
import com.photoconnect.photographer.dto.CreateProfileRequest;
import com.photoconnect.photographer.dto.PhotographerProfileResponse;
import com.photoconnect.photographer.dto.UpdateProfileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PhotographerMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "userId",    ignore = true)
    @Mapping(target = "available", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version",   ignore = true)
    PhotographerProfile toEntity(CreateProfileRequest request);

    PhotographerProfileResponse toResponse(PhotographerProfile profile);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "userId",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version",   ignore = true)
    void updateEntity(UpdateProfileRequest request, @MappingTarget PhotographerProfile profile);
}
