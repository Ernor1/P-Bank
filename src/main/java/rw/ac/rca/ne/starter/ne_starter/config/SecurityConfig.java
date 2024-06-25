package rw.ac.rca.ne.starter.ne_starter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import rw.ac.rca.ne.starter.ne_starter.dtos.responses.CustomAuthError;
import rw.ac.rca.ne.starter.ne_starter.security.jwt.JwtAuthFilter;
import rw.ac.rca.ne.starter.ne_starter.security.user.UserSecurityDetailsService;


@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserSecurityDetailsService userSecurityDetailsService;
    private final CustomAuthError customAuthError;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable().cors().configurationSource(request -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.addAllowedOrigin("*");
                    cors.addAllowedMethod("*");
                    cors.addAllowedHeader("*");
                    return cors;
                }).and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthError)
                .and()
                .authorizeHttpRequests()
                .antMatchers(
                        "/api/v1/auth/**",
                        "/api/v1/analytics/**",
                        "/api/v1/users/create-admin",
                        "/api/v1/importing/**",
                        "/api/v1/location",
                        "/api/v1/roles/**"
                )
                .permitAll()
                .antMatchers(
                        "/v2/api-docs",
                        "/configuration/ui",
                        "/swagger-resources/**",
                        "/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**")
                .permitAll() // the above are the endpoints to the swagger documentation
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userSecurityDetailsService::loadUserByUsername;
    }
}
