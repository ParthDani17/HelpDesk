package com.helpdesk.helpdesk.refreshToken.repository;

import com.helpdesk.helpdesk.refreshToken.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    List<RefreshToken> findAllByUserId(UUID userId);

    @Modifying
    @Query("""
    UPDATE RefreshToken rt
    SET rt.revokedAt = :now
    WHERE rt.user.id = :userId
      AND rt.revokedAt IS NULL
      AND rt.expiresAt > :now
    """)
    int revokeAllActiveTokens(
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );

}
