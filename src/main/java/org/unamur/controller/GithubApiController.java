package org.unamur.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.unamur.api.GithubApi;
import org.unamur.model.GithubRepo;

import java.util.List;
import java.util.Map;

@Slf4j
//@RequiredArgsConstructor
@RestController
public class GithubApiController implements GithubApi {

    private final WebClient githubWebClient;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GithubApiController(WebClient githubWebClient, OAuth2AuthorizedClientService authorizedClientService, OAuth2AuthorizedClientManager authorizedClientManager) {
        this.githubWebClient = githubWebClient;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            return ResponseEntity.ok(oauth2User.getAttributes());
        }
        return ResponseEntity.status(401).build();
    }

    @Override
    public ResponseEntity<List<GithubRepo>> getMyRepos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        List<GithubRepo> repos = githubWebClient.get()
                .uri("/user/repos")
                .retrieve()
                .bodyToFlux(GithubRepo.class)
                .collectList()
                .block();

        return ResponseEntity.ok(repos);
    }
}
