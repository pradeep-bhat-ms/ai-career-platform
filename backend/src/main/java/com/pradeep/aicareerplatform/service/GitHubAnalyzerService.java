package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.GitHubAnalysisDto;
import com.pradeep.aicareerplatform.dto.GitHubProfileDto;
import com.pradeep.aicareerplatform.dto.GitHubRepositoryDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubAnalyzerService {

    // Only matches github.com/<username> (and a trailing slash / extra path segments).
    // Rejects arbitrary URLs, as the spec requires ("do not accept arbitrary URLs").
    private static final Pattern GITHUB_PROFILE_URL_PATTERN =
            Pattern.compile("^https?://(www\\.)?github\\.com/([A-Za-z0-9-]+)/?.*$");

    private final GitHubApiService gitHubApiService;

    public GitHubAnalyzerService(GitHubApiService gitHubApiService) {
        this.gitHubApiService = gitHubApiService;
    }

    public GitHubAnalysisDto analyzeProfile(String profileUrl) {
        String username = extractUsername(profileUrl);

        GitHubProfileDto profile = gitHubApiService.fetchProfile(username);
        List<GitHubRepositoryDto> repositories = gitHubApiService.fetchRepositories(username);

        return new GitHubAnalysisDto(profile, repositories);
    }

    /**
     * @throws InvalidGitHubUrlException if the URL isn't a valid github.com profile URL
     */
    public String extractUsername(String profileUrl) {
        if (profileUrl == null || profileUrl.isBlank()) {
            throw new InvalidGitHubUrlException("GitHub profile URL is required");
        }

        Matcher matcher = GITHUB_PROFILE_URL_PATTERN.matcher(profileUrl.trim());
        if (!matcher.matches()) {
            throw new InvalidGitHubUrlException(
                    "Please enter a valid GitHub profile URL, e.g. https://github.com/username");
        }

        return matcher.group(2);
    }

    public static class InvalidGitHubUrlException extends RuntimeException {
        public InvalidGitHubUrlException(String message) { super(message); }
    }
}