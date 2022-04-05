package api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Disable CSRF (cross site request forgery)
        http.csrf().disable();

        // No session will be created or used by spring security
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Entry points
        http.authorizeRequests()
                .antMatchers("/users/signin").permitAll()
                .antMatchers("/users/signup").permitAll()
                // Disallow everything else..
                .anyRequest().authenticated();

        // Apply JWT
        http.apply(new JwtTokenFilterConfigurer(jwtTokenProvider));

        // Opcional, caso queira que a API tenha uma interface web acessível pelo navegador
        // http.httpBasic();
        // http.exceptionHandling().accessDeniedPage("/login");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Permitir acesso à documentação automática gerada pelo Swagger em http://localhost:8080/swagger-ui/
        web.ignoring()
                .antMatchers("/v2/api-docs")
                .antMatchers("/v3/api-docs")
                .antMatchers("/swagger-resources/**")
                .antMatchers("/swagger-ui/**")
                .antMatchers("/swagger-ui/index.html")
                .antMatchers("/configuration/**")
                .antMatchers("/webjars/**")
                .antMatchers("/public")
                .antMatchers("/swagger-ui.html")
                .antMatchers("/v3/api-docs/**");
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // Adicionar outros authenticationProvider, como implementações de autenticação pelo banco ou pelo ldap. 
        // Mais em https://www.baeldung.com/spring-security-multiple-auth-providers
        //auth.authenticationProvider(customAuthProvider);

        // Adicionar um authenticationProvider "in-memory"
        auth.inMemoryAuthentication()
                .withUser("arpfreitas")
                .password("teste")
                .roles("ADMIN")
                .and()
                .withUser("teste")
                .password("teste")
                .roles("USER");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        //return new BCryptPasswordEncoder(12);
        // Não faz nada com o "password", ou seja, trabalha com texto puro
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
