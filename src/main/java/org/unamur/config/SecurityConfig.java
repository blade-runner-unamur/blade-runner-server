package org.unamur.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenProperties tokenProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // Disable CSRF for APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/ws-metrics/**").permitAll()
                        .requestMatchers("/metrics/**").authenticated() // Protect your endpoint
                        .anyRequest().permitAll() // Maybe change to authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("http://localhost:3000/dashboard", true) // TODO
                )


                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // TODO - Remove ?
                .addFilterBefore(new ApiKeyFilter(tokenProperties.getBackendToken()), UsernamePasswordAuthenticationFilter.class);      // TODO - Remove ?

        return http.build();
    }
}