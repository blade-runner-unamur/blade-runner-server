package org.unamur.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.Map;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenProperties tokenProperties;

    @Bean
    @Profile("!unsecured")
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // Disable CSRF for APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/ws-metrics/**").permitAll()
                        .requestMatchers("/metrics/**", "/github-api/**").authenticated()
                        .anyRequest().permitAll()
                )
//                .oauth2Login(Customizer.withDefaults());
//                .oauth2ResourceServer(Customizer.withDefaults());
                .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Profile("unsecured")
    public SecurityFilterChain unsecuredFilterChain(HttpSecurity http) {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
//                .oauth2Login(Customizer.withDefaults());
                .oauth2ResourceServer(Customizer.withDefaults());
        ;

        // Disable X-Frame-Options because H2 runs inside an iframe.
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    public OpaqueTokenIntrospector opaqueTokenIntrospector() {
        return token -> {
            // This maps the incoming token string to an authenticated user principal
            // You can inspect the 'token' string here if you want to validate it manually!

            Map<String, Object> attributes = Map.of(
                    "sub", "github-user",
                    "token_string", token
            );

            return new DefaultOAuth2AuthenticatedPrincipal(
                    "github-user",
                    attributes,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        };
    }
}