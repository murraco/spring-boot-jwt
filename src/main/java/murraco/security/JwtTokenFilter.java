package murraco.security;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import murraco.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtTokenFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);

  /**
   * Endpoints that authenticate by other means and must stay reachable with an expired access
   * token — refreshing is exactly what a client does once its access token is no longer valid, and
   * clients routinely keep sending the stale Authorization header while doing so.
   */
  private static final Set<String> UNAUTHENTICATED_PATHS =
      Set.of("/users/signin", "/users/signup", "/users/refresh", "/users/logout");

  private final JwtTokenProvider jwtTokenProvider;

  public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    return UNAUTHENTICATED_PATHS.contains(path);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
    String token = jwtTokenProvider.resolveToken(httpServletRequest);
    try {
      if (token != null && jwtTokenProvider.validateToken(token)) {
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (CustomException ex) {
      log.debug("JWT authentication failed: {}", ex.getMessage());
      // this is very important, since it guarantees the user is not authenticated at all
      SecurityContextHolder.clearContext();
      httpServletResponse.sendError(ex.getHttpStatus().value(), ex.getMessage());
      return;
    }

    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }

}
