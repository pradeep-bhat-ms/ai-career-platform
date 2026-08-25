package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class JobDescriptionExtractionDto {
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private String experienceLevel;
    private List<String> responsibilities;
    private String summary;
}