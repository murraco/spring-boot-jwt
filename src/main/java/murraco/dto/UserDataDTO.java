package murraco.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import murraco.model.Role;

import java.util.List;

@Data
public class UserDataDTO {

  @ApiModelProperty(position = 0)
  private String username;
  @ApiModelProperty(position = 1)
  private String email;
  @ApiModelProperty(position = 2)
  private String password;
  @ApiModelProperty(position = 3)
  List<Role> roles;

}
