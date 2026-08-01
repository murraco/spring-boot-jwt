package murraco;

import java.util.ArrayList;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import murraco.model.AppUser;
import murraco.model.AppUserRole;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import murraco.repository.UserRepository;
import murraco.service.UserService;

@SpringBootApplication
@RequiredArgsConstructor
public class JwtAuthServiceApp implements CommandLineRunner {

  private final UserService userService;
  private final UserRepository userRepository;

  public static void main(String[] args) {
    SpringApplication.run(JwtAuthServiceApp.class, args);
  }

  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  @Override
  public void run(String... params) {
    if (!userRepository.existsByUsername("admin")) {
      AppUser admin = new AppUser();
      admin.setUsername("admin");
      admin.setPassword("admin123456");
      admin.setEmail("admin@email.com");
      admin.setAppUserRoles(new ArrayList<AppUserRole>(Arrays.asList(AppUserRole.ROLE_ADMIN)));
      userService.register(admin);
    }

    if (!userRepository.existsByUsername("client")) {
      AppUser client = new AppUser();
      client.setUsername("client");
      client.setPassword("client123456");
      client.setEmail("client@email.com");
      client.setAppUserRoles(new ArrayList<AppUserRole>(Arrays.asList(AppUserRole.ROLE_CLIENT)));
      userService.register(client);
    }
  }

}
