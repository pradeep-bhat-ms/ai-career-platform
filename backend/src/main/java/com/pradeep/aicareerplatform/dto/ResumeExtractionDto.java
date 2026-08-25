package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ResumeExtractionDto {

    private List<String> technicalSkills;
    private List<String> softSkills;
    private String highestEducation;
    private List<String> projects;
    private List<String> certifications;
    private int yearsOfExperience;
    private String summary;
}