package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.GitHubProfileDto;
import com.pradeep.aicareerplatform.dto.GitHubRepositoryDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class GitHubApiService {

    private final WebClient githubWebClient;

    public GitHubApiService(WebClient githubWebClient) {
        this.githubWebClient = githubWebClient;
    }

    /**
     * @throws GitHubUserNotFoundException if the username doesn't exist on GitHub
     * @throws GitHubRateLimitException if the unauthenticated rate limit (60/hr) is exceeded
     * @throws GitHubApiException for any other API failure
     */
    public GitHubProfileDto fetchProfile(String username) {
        return githubWebClient.get()
                .uri("/users/{username}", username)
                .retrieve()
                .bodyToMono(GitHubProfileDto.class)
                .onErrorMap(this::mapError)
                .block();
    }

    public List<GitHubRepositoryDto> fetchRepositories(String username) {
        return githubWebClient.get()
                // per_page=100 covers most profiles in one call; pagination
                // for users with >100 public repos is a later-step concern
                .uri("/users/{username}/repos?per_page=100&sort=updated", username)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GitHubRepositoryDto>>() {})
                .onErrorMap(this::mapError)
                .block();
    }

    private Throwable mapError(Throwable ex) {
        if (ex instanceof WebClientResponseException wcre) {
            if (wcre.getStatusCode().value() == 404) {
                return new GitHubUserNotFoundException("GitHub user not found");
            }
            if (wcre.getStatusCode().value() == 403) {
                // GitHub returns 403 for both rate-limit and abuse-detection;
                // we don't have enough info here to distinguish reliably,
                // so we report it honestly as a possible rate limit rather
                // than guessing.
                return new GitHubRateLimitException(
                        "GitHub API rate limit likely exceeded. Unauthenticated requests are limited to 60/hour.");
            }
        }
        return new GitHubApiException("Failed to reach GitHub API: " + ex.getMessage());
    }

    public static class GitHubUserNotFoundException extends RuntimeException {
        public GitHubUserNotFoundException(String message) { super(message); }
    }

    public static class GitHubRateLimitException extends RuntimeException {
        public GitHubRateLimitException(String message) { super(message); }
    }

    public static class GitHubApiException extends RuntimeException {
        public GitHubApiException(String message) { super(message); }
    }
}