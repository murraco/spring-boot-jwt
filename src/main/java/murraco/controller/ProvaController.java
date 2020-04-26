package murraco.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import murraco.dto.UserResponseDTO;

@RestController
@RequestMapping("/prova")
@Api(tags = "prova")
public class ProvaController {

  

  @Autowired
  private ModelMapper modelMapper;


  @GetMapping(value = "/test")
  @ApiOperation(value = "esempio di prova", response = UserResponseDTO.class)
  @ApiResponses(value = {//
      @ApiResponse(code = 400, message = "Something went wrong"), //
      @ApiResponse(code = 403, message = "Access denied"), //
      @ApiResponse(code = 404, message = "The user doesn't exist"), //
      @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
  public UserResponseDTO test() {
	  UserResponseDTO userResponseDTO = new UserResponseDTO();
	  userResponseDTO.setUsername("vale");
	  userResponseDTO.setId(1);
	  userResponseDTO.setEmail("va@tim.it");
	  
	  
	  return userResponseDTO;
	  
  }
  
  
  @GetMapping(value = "/test1")
  @ApiOperation(value = "esempio di prova", response = UserResponseDTO.class)
  @ApiResponses(value = {//
      @ApiResponse(code = 400, message = "Something went wrong"), //
      @ApiResponse(code = 403, message = "Access denied"), //
      @ApiResponse(code = 404, message = "The user doesn't exist"), //
      @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
  public UserResponseDTO test(@ApiParam("Username") @PathVariable String username) {
    
	  UserResponseDTO userResponseDTO = new UserResponseDTO();
	  userResponseDTO.setUsername(username);
	  userResponseDTO.setId(1);
	  userResponseDTO.setEmail("va@tim.it");
	  
	  return userResponseDTO;
	  
  }

  

}
