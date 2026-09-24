package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Maps directly to GitHub's GET /users/{username} response.
 * Field names match GitHub's JSON so Jackson can deserialize without
 * custom mapping. Only fields we actually use are included — GitHub
 * returns more than this.
 */
@Getter
@Setter
public class GitHubProfileDto {
    private String login;              // username
    private String name;
    private String avatar_url;
    private String bio;
    private String location;
    private String company;
    private String blog;
    private String html_url;           // profile URL
    private int followers;
    private int following;
    private int public_repos;
    private String created_at;
}