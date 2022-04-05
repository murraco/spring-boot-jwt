package api.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/**")
                //.pathsToExclude("/actuator/**") //exclui o grupo abaixo
                .build();
    }

    /* posso criar grupos de api
    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group("actuators")
                .pathsToMatch("/actuators/**")
                .build();
    }
     */
    @Bean
    public OpenAPI springShopOpenAPI() {

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("jwt",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER).name("Authorization")))
                .info(info()) // .addSecurityItem(new SecurityRequirement().addList("bearer-jwt", Arrays.asList("read", "write"))) //isso adiciona obrigatoriedade de jwt em todas requisições
                ;
    }

    private Info info() {
        return new Info()
                .title("API em SpringBoot e JWT")
                .description("Esse sistema é baseado no [https://github.com/murraco/spring-boot-jwt](Spring Boot JWT by murraco). For this sample, you can use the `admin` or `client` users (password: admin and client respectively) to test the authorization filters. Once you have successfully logged in and obtained the token, you should click on the right top button `Authorize` and introduce it with the prefix \"Bearer \".")
                .version("1.0.0")
                .license(new License().name("Mit lisense").url("http://google.com"))
                .contact(new Contact().name("Alison").email("arpfreitas@uem.br"));
    }
}
