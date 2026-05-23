package com.photoconnect.auth.mapper;

import com.photoconnect.auth.domain.Role;
import com.photoconnect.auth.domain.User;
import com.photoconnect.auth.dto.UserDto;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-23T23:13:07+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String email = null;
        Role role = null;
        boolean enabled = false;
        Instant createdAt = null;

        id = user.getId();
        email = user.getEmail();
        role = user.getRole();
        enabled = user.isEnabled();
        createdAt = user.getCreatedAt();

        UserDto userDto = new UserDto( id, email, role, enabled, createdAt );

        return userDto;
    }
}
