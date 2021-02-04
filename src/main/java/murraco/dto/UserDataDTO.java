package murraco.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import murraco.model.Role;
import murraco.util.annotation.ValidPassword;

import javax.validation.constraints.Email;
import java.util.List;

@Data
public class UserDataDTO {

  @ApiModelProperty(position = 0)
  private String username;

  @ApiModelProperty(position = 1)
  @Email(message = "Email not valid")
  private String email;

  @ApiModelProperty(position = 2)
  @ValidPassword
  private String password;

  @ApiModelProperty(position = 3)
  List<Role> roles;

}
