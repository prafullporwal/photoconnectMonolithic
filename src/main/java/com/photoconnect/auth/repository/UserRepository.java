package com.photoconnect.auth.repository;

import com.photoconnect.auth.domain.Role;
import com.photoconnect.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

    List<User> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    long countByRoleAndDeletedAtIsNull(Role role);
}
