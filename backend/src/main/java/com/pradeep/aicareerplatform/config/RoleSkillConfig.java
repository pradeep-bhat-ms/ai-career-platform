package com.pradeep.aicareerplatform.config;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class RoleSkillConfig {

    public record RoleSkills(List<String> required, List<String> recommended, List<String> optional) {}

    private final Map<String, RoleSkills> roleSkillMap = Map.of(
            "Java Full Stack Developer", new RoleSkills(
                    List.of("Java", "Spring Boot", "React", "REST API", "SQL"),
                    List.of("Docker", "JUnit", "Mockito", "Microservices"),
                    List.of("AWS", "Kubernetes")
            ),
            "Backend Developer", new RoleSkills(
                    List.of("Java", "Spring Boot", "SQL", "REST API", "Hibernate"),
                    List.of("Docker", "Microservices", "JUnit", "Redis"),
                    List.of("Kafka", "AWS")
            ),
            "Frontend Developer", new RoleSkills(
                    List.of("HTML5", "CSS3", "JavaScript", "React", "REST API"),
                    List.of("TypeScript", "Redux", "Jest"),
                    List.of("Next.js", "Tailwind CSS")
            ),
            "Data Analyst", new RoleSkills(
                    List.of("SQL", "Python", "Excel", "Data Visualization"),
                    List.of("Power BI", "Tableau", "Statistics"),
                    List.of("Machine Learning", "R")
            ),
            "DevOps Engineer", new RoleSkills(
                    List.of("Docker", "Kubernetes", "CI/CD", "Linux"),
                    List.of("AWS", "Jenkins", "Terraform"),
                    List.of("Ansible", "Monitoring Tools")
            ),
            "Software Engineer", new RoleSkills(
                    List.of("Java", "Data Structures", "Algorithms", "SQL"),
                    List.of("Spring Boot", "Git", "REST API"),
                    List.of("Docker", "Cloud Basics")
            )
    );

    public RoleSkills getSkillsForRole(String role) {
        RoleSkills skills = roleSkillMap.get(role);
        if (skills == null) {
            throw new IllegalArgumentException("Unknown target role: " + role);
        }
        return skills;
    }

    public List<String> getAvailableRoles() {
        return roleSkillMap.keySet().stream().sorted().toList();
    }
}