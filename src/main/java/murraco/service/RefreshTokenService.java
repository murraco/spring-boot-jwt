package murraco.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import murraco.exception.CustomException;
import murraco.model.RefreshToken;
import murraco.repository.RefreshTokenRepository;

/**
 * Issues, rotates and revokes refresh tokens.
 *
 * <p>Refresh tokens are opaque random strings rather than JWTs: they are checked against the
 * database on every use, which is what makes them revocable. Only their hash is persisted.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

  private static final int TOKEN_BYTES = 32;

  private final RefreshTokenRepository refreshTokenRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  @Value("${security.jwt.refresh-token.expire-length:604800000}")
  private long refreshValidityInMilliseconds;

  /** Creates a new refresh token for the user and returns the raw value (never stored). */
  @Transactional
  public String issue(String username) {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setTokenHash(hash(rawToken));
    refreshToken.setUsername(username);
    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshValidityInMilliseconds));
    refreshToken.setRevoked(false);
    refreshTokenRepository.save(refreshToken);

    return rawToken;
  }

  /**
   * Validates the presented refresh token and rotates it: the old token is revoked and a new one
   * issued. Returns the username the token belongs to, along with its replacement.
   *
   * <p>Presenting an already-revoked token is treated as a possible token theft (the legitimate
   * client and an attacker both hold a copy), so every token for that user is revoked.
   */
  // dontRollbackOn: the theft path revokes the user's tokens and *then* rejects the request. Without
  // this, throwing would roll the revocation back and the stolen tokens would stay usable.
  @Transactional(dontRollbackOn = CustomException.class)
  public Rotation rotate(String rawToken) {
    Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(hash(rawToken));
    if (found.isEmpty()) {
      throw new CustomException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }

    RefreshToken refreshToken = found.get();
    if (refreshToken.isRevoked()) {
      log.warn("Refresh token reuse detected for user {}; revoking all tokens", refreshToken.getUsername());
      refreshTokenRepository.revokeAllByUsername(refreshToken.getUsername());
      throw new CustomException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }
    if (refreshToken.isExpired()) {
      throw new CustomException("Expired refresh token", HttpStatus.UNAUTHORIZED);
    }

    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);

    return new Rotation(refreshToken.getUsername(), issue(refreshToken.getUsername()));
  }

  /** Revokes the presented token. Unknown tokens are ignored so logout is always idempotent. */
  @Transactional
  public void revoke(String rawToken) {
    refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(refreshToken -> {
      refreshToken.setRevoked(true);
      refreshTokenRepository.save(refreshToken);
    });
  }

  /** Removes every token belonging to a user, e.g. when the account is deleted. */
  @Transactional
  public void deleteAllForUser(String username) {
    refreshTokenRepository.deleteByUsername(username);
  }

  private String hash(String rawToken) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  /** The user a rotated token belonged to, and the replacement token to hand back to the client. */
  public record Rotation(String username, String newRefreshToken) {
  }

}
