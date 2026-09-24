package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubLicenseDto {
    private String key;    // e.g. "mit"
    private String name;   // e.g. "MIT License"
    private String spdx_id;
}