package com.pradeep.aicareerplatform.util;

import java.util.List;

public class SkillMatchUtil {

    public static boolean matches(List<String> candidateSkills, String targetSkill) {
        String normalizedTarget = normalize(targetSkill);
        return candidateSkills.stream().anyMatch(skill -> {
            String normalizedSkill = normalize(skill);
            return normalizedSkill.contains(normalizedTarget) || normalizedTarget.contains(normalizedSkill);
        });
    }

    public static String normalize(String skill) {
        return skill.toLowerCase()
                .replaceAll("s$", "")
                .replaceAll("[.\\-_]", "");
    }
}