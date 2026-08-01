package murraco.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.SecretKey;

import lombok.RequiredArgsConstructor;
import murraco.model.AppUserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import murraco.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${security.jwt.token.secret-key:secret-key}")
  private String secretKey;

  @Value("${security.jwt.token.expire-length:3600000}")
  private long validityInMilliseconds = 3600000;

  private final MyUserDetails myUserDetails;

  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

  private SecretKey signingKey;

  @PostConstruct
  protected void init() {
    try {
      byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKey.getBytes(StandardCharsets.UTF_8));
      signingKey = Keys.hmacShaKeyFor(keyBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public String createToken(String username, List<AppUserRole> appUserRoles) {
    List<String> roleNames = appUserRoles.stream().map(AppUserRole::name).collect(Collectors.toList());
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);
    return Jwts.builder()
        .subject(username)
        .claim("auth", roleNames)
        .issuedAt(now)
        .expiration(validity)
        .signWith(signingKey)
        .compact();
  }

  /** Access token lifetime in seconds, as reported to clients in the auth response. */
  public long getValidityInSeconds() {
    return validityInMilliseconds / 1000;
  }

  public Authentication getAuthentication(String token) {
    UserDetails userDetails = myUserDetails.loadUserByUsername(getUsername(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getUsername(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return claims.getSubject();
  }

  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(signingKey)
          .build()
          .parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Invalid JWT token", e);
      throw new CustomException("Expired or invalid JWT token", HttpStatus.UNAUTHORIZED);
    }
  }

}
