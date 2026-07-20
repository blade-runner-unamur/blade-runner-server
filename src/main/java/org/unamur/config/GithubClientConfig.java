package org.unamur.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import javax.inject.Named;

@Configuration
public class GithubClientConfig {

    @Bean
    @Profile("!unsecured")
    @Named("githubWebClient")
    public WebClient githubWebClient(OAuth2AuthorizedClientManager authorizedClientManager, TokenProperties tokenProperties) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("github");

        return WebClient.builder()
                .baseUrl("https://api.github.com")
//                .apply(oauth2Client.oauth2Configuration())
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .defaultHeader(org.springframework.http.HttpHeaders.USER_AGENT, "Spring-Boot-App")
                .defaultHeader(org.springframework.http.HttpHeaders.AUTHORIZATION, "Bearer " + tokenProperties.getGithubPat())
                .build();
    }

    @Bean
    @Profile("unsecured")
    @Named("unsecuredGithubWebClient")
    public WebClient unsecuredGithubWebClient(TokenProperties tokenProperties) {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl("https://api.github.com")
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(10 * 1024 * 1024));
        String githubPat = tokenProperties.getGithubPat();
        if (githubPat != null && !githubPat.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubPat);
        }
        return builder.build();
    }
}
