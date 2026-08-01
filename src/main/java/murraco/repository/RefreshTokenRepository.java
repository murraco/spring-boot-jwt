package murraco.repository;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import murraco.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  @Transactional
  void deleteByUsername(String username);

  @Transactional
  @Modifying
  @Query("update RefreshToken t set t.revoked = true where t.username = :username and t.revoked = false")
  int revokeAllByUsername(String username);

}
