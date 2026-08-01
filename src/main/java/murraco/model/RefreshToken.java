package murraco.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A refresh token issued to a user. Only the SHA-256 hash of the token is stored, so a database
 * leak does not hand out usable tokens. Tokens are single-use: refreshing revokes the presented
 * token and issues a new one.
 */
@Entity
@Table(indexes = @Index(name = "idx_refresh_token_username", columnList = "username"))
@Data
@NoArgsConstructor
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(unique = true, nullable = false, length = 64)
  private String tokenHash;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private Instant expiryDate;

  @Column(nullable = false)
  private boolean revoked;

  public boolean isExpired() {
    return expiryDate.isBefore(Instant.now());
  }

}
