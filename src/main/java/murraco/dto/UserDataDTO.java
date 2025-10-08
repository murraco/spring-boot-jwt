package murraco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import murraco.model.AppUserRole;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Data
@NoArgsConstructor
public class UserDataDTO {

    @Schema(accessMode = READ_ONLY)
  List<AppUserRole> appUserRoles;
    @Schema(accessMode = READ_ONLY)
  private String username;
    @Schema(accessMode = READ_ONLY)
  private String email;
    @Schema(accessMode = READ_ONLY)
  private String password;

}
