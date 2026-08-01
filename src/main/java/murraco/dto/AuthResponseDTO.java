package murraco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The token pair returned by signin, signup and refresh. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

  @Schema(description = "Short-lived JWT to send as 'Authorization: Bearer <token>'")
  private String accessToken;

  @Schema(description = "Long-lived opaque token used to obtain a new access token")
  private String refreshToken;

  @Schema(description = "Authentication scheme the access token is used with", example = "Bearer")
  private String tokenType;

  @Schema(description = "Access token lifetime in seconds")
  private long expiresIn;

}
