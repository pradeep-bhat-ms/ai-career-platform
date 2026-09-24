package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GitHubAnalysisDto {
    private GitHubProfileDto profile;
    private List<GitHubRepositoryDto> repositories;
    // Later steps add: languages, contributions, openSource, deployments,
    // licenses, documentation, engineeringSignals, improvements — kept out
    // of Step 1 on purpose so this stays honest about what's implemented.
}