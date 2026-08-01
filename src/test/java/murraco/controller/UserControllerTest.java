package murraco.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASSWORD = "admin123456";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /** Signs in and returns the token pair as {accessToken, refreshToken, ...}. */
  private JsonNode signin() throws Exception {
    String body = mockMvc.perform(post("/users/signin")
            .param("username", ADMIN_USER)
            .param("password", ADMIN_PASSWORD))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    return objectMapper.readTree(body);
  }

  private String refreshBody(String refreshToken) throws Exception {
    return objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken));
  }

  @Test
  void signin_withValidCredentials_returnsTokenPair() throws Exception {
    JsonNode tokens = signin();
    assertTrue(tokens.get("accessToken").asText().length() > 20, "Expected a JWT access token");
    assertTrue(tokens.get("refreshToken").asText().length() > 20, "Expected a refresh token");
    assertTrue(tokens.get("expiresIn").asLong() > 0, "Expected a positive access token lifetime");
  }

  @Test
  void me_withoutToken_returns403() throws Exception {
    mockMvc.perform(get("/users/me"))
        .andExpect(status().isForbidden());
  }

  @Test
  void me_withValidToken_returnsUserData() throws Exception {
    String accessToken = signin().get("accessToken").asText();

    mockMvc.perform(get("/users/me")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(ADMIN_USER))
        .andExpect(jsonPath("$.email").value("admin@email.com"))
        .andExpect(jsonPath("$.appUserRoles").isArray());
  }

  @Test
  void refresh_withValidRefreshToken_returnsNewPair() throws Exception {
    JsonNode tokens = signin();
    String refreshToken = tokens.get("refreshToken").asText();

    String body = mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(refreshToken)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode refreshed = objectMapper.readTree(body);
    assertTrue(refreshed.get("accessToken").asText().length() > 20, "Expected a new access token");
    assertNotEquals(refreshToken, refreshed.get("refreshToken").asText(), "Refresh token should rotate");

    // The new access token must actually work.
    mockMvc.perform(get("/users/me")
            .header("Authorization", "Bearer " + refreshed.get("accessToken").asText()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(ADMIN_USER));
  }

  @Test
  void refresh_worksWithoutAccessToken() throws Exception {
    // The whole point of a refresh token: it works when the access token is expired or bogus.
    String refreshToken = signin().get("refreshToken").asText();

    mockMvc.perform(post("/users/refresh")
            .header("Authorization", "Bearer expired-and-invalid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(refreshToken)))
        .andExpect(status().isOk());
  }

  @Test
  void refresh_reusingRotatedToken_returns401() throws Exception {
    String refreshToken = signin().get("refreshToken").asText();

    mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(refreshToken)))
        .andExpect(status().isOk());

    // Second use of the same token is a reuse attempt and must be rejected.
    mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(refreshToken)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refresh_afterReuseDetection_revokesRemainingTokens() throws Exception {
    String refreshToken = signin().get("refreshToken").asText();

    String body = mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(refreshToken)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    String rotated = objectMapper.readTree(body).get("refreshToken").asText();

    // Replaying the consumed token signals theft, which invalidates the whole family.
    mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(refreshToken)))
        .andExpect(status().isUnauthorized());

    mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(rotated)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refresh_withUnknownToken_returns401() throws Exception {
    mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody("not-a-real-refresh-token")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refresh_withBlankToken_returns400() throws Exception {
    mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody("")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void logout_revokesRefreshToken() throws Exception {
    String refreshToken = signin().get("refreshToken").asText();

    mockMvc.perform(post("/users/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(refreshToken)))
        .andExpect(status().isNoContent());

    mockMvc.perform(post("/users/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody(refreshToken)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logout_withUnknownToken_isIdempotent() throws Exception {
    mockMvc.perform(post("/users/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(refreshBody("never-issued")))
        .andExpect(status().isNoContent());
  }

  @Test
  void signup_returnsTokenPair() throws Exception {
    String body = """
        {"username":"newuser","email":"newuser@example.com","password":"password12","appUserRoles":["ROLE_CLIENT"]}
        """;
    String response = mockMvc.perform(post("/users/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode tokens = objectMapper.readTree(response);
    assertTrue(tokens.get("accessToken").asText().length() > 20, "Expected a JWT access token");
    assertTrue(tokens.get("refreshToken").asText().length() > 20, "Expected a refresh token");
  }

  @Test
  void signup_duplicateUsername_returns422() throws Exception {
    String body = """
        {"username":"admin","email":"other@example.com","password":"password12","appUserRoles":["ROLE_CLIENT"]}
        """;
    mockMvc.perform(post("/users/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void me_withMalformedToken_returns401() throws Exception {
    mockMvc.perform(get("/users/me")
            .header("Authorization", "Bearer not-a-valid-jwt"))
        .andExpect(status().isUnauthorized());
  }
}
