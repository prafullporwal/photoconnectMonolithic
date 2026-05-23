package com.photoconnect.photographer.mapper;

import com.photoconnect.photographer.domain.PhotographerProfile;
import com.photoconnect.photographer.dto.CreateProfileRequest;
import com.photoconnect.photographer.dto.PhotographerProfileResponse;
import com.photoconnect.photographer.dto.UpdateProfileRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-23T18:52:16+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PhotographerMapperImpl implements PhotographerMapper {

    @Override
    public PhotographerProfile toEntity(CreateProfileRequest request) {
        if ( request == null ) {
            return null;
        }

        PhotographerProfile photographerProfile = new PhotographerProfile();

        photographerProfile.setBio( request.bio() );
        photographerProfile.setDisplayName( request.displayName() );
        photographerProfile.setLocation( request.location() );
        photographerProfile.setPricePerHour( request.pricePerHour() );
        List<String> list = request.specialties();
        if ( list != null ) {
            photographerProfile.setSpecialties( new ArrayList<String>( list ) );
        }
        if ( request.yearsOfExperience() != null ) {
            photographerProfile.setYearsOfExperience( request.yearsOfExperience() );
        }

        photographerProfile.setAvailable( true );

        return photographerProfile;
    }

    @Override
    public PhotographerProfileResponse toResponse(PhotographerProfile profile) {
        if ( profile == null ) {
            return null;
        }

        UUID id = null;
        UUID userId = null;
        String displayName = null;
        String bio = null;
        String location = null;
        int yearsOfExperience = 0;
        BigDecimal pricePerHour = null;
        boolean available = false;
        List<String> specialties = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = profile.getId();
        userId = profile.getUserId();
        displayName = profile.getDisplayName();
        bio = profile.getBio();
        location = profile.getLocation();
        yearsOfExperience = profile.getYearsOfExperience();
        pricePerHour = profile.getPricePerHour();
        available = profile.isAvailable();
        List<String> list = profile.getSpecialties();
        if ( list != null ) {
            specialties = new ArrayList<String>( list );
        }
        createdAt = profile.getCreatedAt();
        updatedAt = profile.getUpdatedAt();

        PhotographerProfileResponse photographerProfileResponse = new PhotographerProfileResponse( id, userId, displayName, bio, location, yearsOfExperience, pricePerHour, available, specialties, createdAt, updatedAt );

        return photographerProfileResponse;
    }

    @Override
    public void updateEntity(UpdateProfileRequest request, PhotographerProfile profile) {
        if ( request == null ) {
            return;
        }

        profile.setAvailable( request.available() );
        profile.setBio( request.bio() );
        profile.setDisplayName( request.displayName() );
        profile.setLocation( request.location() );
        profile.setPricePerHour( request.pricePerHour() );
        if ( profile.getSpecialties() != null ) {
            List<String> list = request.specialties();
            if ( list != null ) {
                profile.getSpecialties().clear();
                profile.getSpecialties().addAll( list );
            }
            else {
                profile.setSpecialties( null );
            }
        }
        else {
            List<String> list = request.specialties();
            if ( list != null ) {
                profile.setSpecialties( new ArrayList<String>( list ) );
            }
        }
        if ( request.yearsOfExperience() != null ) {
            profile.setYearsOfExperience( request.yearsOfExperience() );
        }
    }
}
