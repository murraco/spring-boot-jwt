package murraco.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import murraco.model.AppUser;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import murraco.dto.AuthResponseDTO;
import murraco.dto.RefreshRequestDTO;
import murraco.dto.UserDataDTO;
import murraco.dto.UserResponseDTO;
import murraco.service.UserService;

@RestController
@RequestMapping("/users")
@Tag(name = "users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final ModelMapper modelMapper;

  @PostMapping("/signin")
  @Operation(summary = "Authenticates user and returns an access/refresh token pair")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "422", description = "Invalid username/password supplied")})
  public AuthResponseDTO login(
      @Parameter(description = "Username") @RequestParam String username,
      @Parameter(description = "Password") @RequestParam String password) {
    return userService.signin(username, password);
  }

  @PostMapping("/signup")
  @Operation(summary = "Creates user and returns an access/refresh token pair")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "422", description = "Username is already in use")})
  public AuthResponseDTO signup(@Parameter(description = "Signup User") @RequestBody @Valid UserDataDTO user) {
    return userService.signup(modelMapper.map(user, AppUser.class));
  }

  @DeleteMapping(value = "/{username}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Operation(summary = "Deletes user by username")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "The user doesn't exist"),
      @ApiResponse(responseCode = "401", description = "Expired or invalid JWT token")})
  public String delete(@Parameter(description = "Username") @PathVariable String username) {
    userService.delete(username);
    return username;
  }

  @GetMapping(value = "/{username}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Operation(summary = "Returns user by username")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "The user doesn't exist"),
      @ApiResponse(responseCode = "401", description = "Expired or invalid JWT token")})
  public UserResponseDTO search(@Parameter(description = "Username") @PathVariable String username) {
    return modelMapper.map(userService.search(username), UserResponseDTO.class);
  }

  @GetMapping(value = "/me")
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
  @Operation(summary = "Returns the authenticated user's data")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "401", description = "Expired or invalid JWT token")})
  public UserResponseDTO whoami(HttpServletRequest req) {
    return modelMapper.map(userService.whoami(req), UserResponseDTO.class);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Exchanges a refresh token for a new access/refresh token pair",
      description = "Does not require an access token, so it works after the access token has expired. "
          + "The presented refresh token is consumed and replaced by the one in the response.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "New token pair issued"),
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "401", description = "Expired or invalid refresh token"),
      @ApiResponse(responseCode = "404", description = "User no longer exists")})
  public AuthResponseDTO refresh(@RequestBody @Valid RefreshRequestDTO request) {
    return userService.refresh(request.getRefreshToken());
  }

  @PostMapping("/logout")
  @Operation(summary = "Revokes a refresh token",
      description = "Idempotent: unknown or already-revoked tokens also return 204. The matching "
          + "access token stays valid until it expires.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Refresh token revoked"),
      @ApiResponse(responseCode = "400", description = "Something went wrong")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@RequestBody @Valid RefreshRequestDTO request) {
    userService.logout(request.getRefreshToken());
  }

}
