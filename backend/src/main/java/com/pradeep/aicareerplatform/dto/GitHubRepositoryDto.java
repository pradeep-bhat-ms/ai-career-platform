package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Maps directly to entries from GET /users/{username}/repos.
 * Extended with more fields in later steps (README, deployment, etc.) —
 * kept minimal here for Step 1.
 */
@Getter
@Setter
public class GitHubRepositoryDto {
    private String name;
    private String description;
    private String html_url;
    private String language;
    private int stargazers_count;
    private int forks_count;
    private int open_issues_count;
    private String created_at;
    private String updated_at;
    private String default_branch;
    private boolean archived;
    private boolean fork;
    private String visibility;
    private List<String> topics;
    private GitHubLicenseDto license; // null if no license detected
    private String homepage;          // used later for deployment detection
}