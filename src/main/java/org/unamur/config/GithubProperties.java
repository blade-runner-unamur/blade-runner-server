package org.unamur.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.github")
@Data
public class GithubProperties {
    private String clientId;
    private String clientSecret;
    private String scope;
}
