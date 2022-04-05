package api.controller;

import api.dto.LoginRequestDTO;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import api.model.AppUser;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name="users") //Personalizando o Swagger https://springdoc.org/
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @PostMapping("/signin")
    public String login(@Parameter(name="Signin parameters") @RequestBody LoginRequestDTO loginRequest) {
        return userService.signin(loginRequest.getUsername(), loginRequest.getPassword());
    }

    @PostMapping("/signup")
    public String signup(@Parameter(name="Signup User") @RequestBody AppUser user) {
        return userService.signup(user);
    }

    @DeleteMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //Exemplo de como personalizar as informações do Swagger
    @Operation(summary = "Deletar",description = "Apaga um usuário", security = @SecurityRequirement(name = "jwt"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Alguma coisa deu errado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "O usuário não existe"),
        @ApiResponse(responseCode = "500", description = "Expired or invalid JWT token")})
    public String delete(@Parameter(name="Username") @PathVariable String username) {
        userService.delete(username);
        return username;
    }

    @GetMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AppUser search(@Parameter(name="Username") @PathVariable String username) {
        return userService.search(username);
    }

    @GetMapping(value = "/me")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public AppUser whoami(HttpServletRequest req) {
        return userService.whoami(req);
    }

    @GetMapping("/refresh")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    public String refresh(HttpServletRequest req) {
        return userService.refresh(req.getRemoteUser());
    }

}
