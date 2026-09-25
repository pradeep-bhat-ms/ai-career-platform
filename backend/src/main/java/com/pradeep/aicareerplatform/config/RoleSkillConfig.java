package com.pradeep.aicareerplatform.config;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RoleSkillConfig {

    public record RoleSkills(
            List<String> required,
            List<String> recommended,
            List<String> optional
    ) {}

    private final Map<String, RoleSkills> roleSkillMap = new HashMap<>();

    public RoleSkillConfig() {

        roleSkillMap.put("Java Full Stack Developer", new RoleSkills(
                List.of(
                        "Java",
                        "OOP",
                        "Spring Boot",
                        "Spring Security",
                        "Hibernate",
                        "JPA",
                        "REST API",
                        "SQL",
                        "HTML5",
                        "CSS3",
                        "JavaScript",
                        "React"
                ),
                List.of(
                        "MySQL",
                        "PostgreSQL",
                        "JWT",
                        "Maven",
                        "Git",
                        "GitHub",
                        "JUnit",
                        "Mockito",
                        "Bootstrap",
                        "Thymeleaf"
                ),
                List.of(
                        "Microservices",
                        "Docker",
                        "AWS",
                        "Kubernetes",
                        "Kafka",
                        "Redis"
                )
        ));

        roleSkillMap.put("Software Engineer", new RoleSkills(
                List.of(
                        "Java",
                        "OOP",
                        "Data Structures",
                        "Algorithms",
                        "SQL"
                ),
                List.of(
                        "Spring Boot",
                        "REST API",
                        "Git",
                        "GitHub",
                        "JUnit",
                        "Maven"
                ),
                List.of(
                        "Docker",
                        "Microservices",
                        "AWS",
                        "System Design"
                )
        ));

        roleSkillMap.put("Backend Developer", new RoleSkills(
                List.of(
                        "Java",
                        "Spring Boot",
                        "Spring Security",
                        "Hibernate",
                        "JPA",
                        "SQL",
                        "REST API"
                ),
                List.of(
                        "MySQL",
                        "PostgreSQL",
                        "JWT",
                        "Maven",
                        "Git",
                        "JUnit",
                        "Mockito"
                ),
                List.of(
                        "Docker",
                        "Microservices",
                        "Redis",
                        "Kafka",
                        "AWS"
                )
        ));

        roleSkillMap.put("Frontend Developer", new RoleSkills(
                List.of(
                        "HTML5",
                        "CSS3",
                        "JavaScript",
                        "React"
                ),
                List.of(
                        "TypeScript",
                        "Redux",
                        "REST API",
                        "Git",
                        "Bootstrap",
                        "Jest"
                ),
                List.of(
                        "Next.js",
                        "Tailwind CSS"
                )
        ));

        roleSkillMap.put("Full Stack Web Developer (MERN)", new RoleSkills(
                List.of(
                        "MongoDB",
                        "Express",
                        "React",
                        "Node.js",
                        "JavaScript"
                ),
                List.of(
                        "REST API",
                        "Git",
                        "GitHub",
                        "JWT",
                        "HTML5",
                        "CSS3"
                ),
                List.of(
                        "Docker",
                        "AWS",
                        "Tailwind CSS"
                )
        ));

        roleSkillMap.put("DevOps Engineer", new RoleSkills(
                List.of(
                        "Docker",
                        "Kubernetes",
                        "CI/CD",
                        "Linux"
                ),
                List.of(
                        "AWS",
                        "Jenkins",
                        "Terraform",
                        "Git",
                        "GitHub"
                ),
                List.of(
                        "Ansible",
                        "Prometheus",
                        "Grafana"
                )
        ));

        roleSkillMap.put("Data Analyst", new RoleSkills(
                List.of(
                        "SQL",
                        "Python",
                        "Excel",
                        "Data Visualization"
                ),
                List.of(
                        "Power BI",
                        "Tableau",
                        "Statistics",
                        "Pandas"
                ),
                List.of(
                        "Machine Learning",
                        "R"
                )
        ));

        roleSkillMap.put("Data Scientist / AI Engineer", new RoleSkills(
                List.of(
                        "Python",
                        "Machine Learning",
                        "Deep Learning",
                        "SQL"
                ),
                List.of(
                        "PyTorch",
                        "TensorFlow",
                        "Scikit-Learn",
                        "Pandas",
                        "NLP"
                ),
                List.of(
                        "Docker",
                        "MLflow",
                        "AWS"
                )
        ));

        roleSkillMap.put("Machine Learning Engineer", new RoleSkills(
                List.of(
                        "Python",
                        "Machine Learning",
                        "Algorithms",
                        "SQL"
                ),
                List.of(
                        "PyTorch",
                        "TensorFlow",
                        "Scikit-Learn",
                        "FastAPI",
                        "Git",
                        "MLOps"
                ),
                List.of(
                        "Docker",
                        "Kubernetes",
                        "ONNX"
                )
        ));

        roleSkillMap.put("Cloud & Systems Engineer", new RoleSkills(
                List.of(
                        "Linux",
                        "AWS",
                        "Networking",
                        "Bash",
                        "Security"
                ),
                List.of(
                        "Docker",
                        "Terraform",
                        "Python",
                        "Git",
                        "System Design"
                ),
                List.of(
                        "Kubernetes",
                        "Ansible"
                )
        ));

        roleSkillMap.put("Mobile App Developer (Android / iOS)", new RoleSkills(
                List.of(
                        "Java",
                        "Kotlin",
                        "Android Studio",
                        "Git",
                        "REST API"
                ),
                List.of(
                        "SQLite",
                        "Firebase",
                        "XML",
                        "MVVM"
                ),
                List.of(
                        "Flutter",
                        "Jetpack Compose"
                )
        ));

        roleSkillMap.put("Cybersecurity Engineer", new RoleSkills(
                List.of(
                        "Network Security",
                        "Ethical Hacking",
                        "Linux",
                        "Cryptography",
                        "Firewalls"
                ),
                List.of(
                        "Python",
                        "SIEM",
                        "Penetration Testing",
                        "Wireshark"
                ),
                List.of(
                        "CISSP",
                        "OWASP"
                )
        ));

        roleSkillMap.put("QA Automation Engineer", new RoleSkills(
                List.of(
                        "Java",
                        "Selenium",
                        "TestNG",
                        "Automation Testing",
                        "Git"
                ),
                List.of(
                        "JUnit",
                        "REST Assured",
                        "Cucumber",
                        "Maven",
                        "CI/CD"
                ),
                List.of(
                        "Docker",
                        "Postman"
                )
        ));

        roleSkillMap.put("Data Engineer", new RoleSkills(
                List.of(
                        "Python",
                        "SQL",
                        "Spark",
                        "Data Pipelines",
                        "ETL"
                ),
                List.of(
                        "Hadoop",
                        "Airflow",
                        "PostgreSQL",
                        "Kafka"
                ),
                List.of(
                        "Snowflake",
                        "Docker"
                )
        ));

        roleSkillMap.put("UI/UX Designer", new RoleSkills(
                List.of(
                        "Figma",
                        "Wireframing",
                        "Prototyping",
                        "User Research",
                        "UI Design"
                ),
                List.of(
                        "Design Systems",
                        "Usability Testing",
                        "HTML5",
                        "CSS3"
                ),
                List.of(
                        "Adobe XD",
                        "Accessibility"
                )
        ));

        roleSkillMap.put("Product Manager", new RoleSkills(
                List.of(
                        "Product Strategy",
                        "Agile",
                        "User Stories",
                        "Roadmapping",
                        "Analytics"
                ),
                List.of(
                        "Jira",
                        "Market Research",
                        "A/B Testing",
                        "UX Foundations"
                ),
                List.of(
                        "SQL",
                        "Scrum"
                )
        ));

        roleSkillMap.put("Database Administrator", new RoleSkills(
                List.of(
                        "SQL",
                        "MySQL",
                        "PostgreSQL",
                        "Database Tuning",
                        "Backup & Recovery"
                ),
                List.of(
                        "Linux",
                        "PL/SQL",
                        "Replication",
                        "Security"
                ),
                List.of(
                        "MongoDB",
                        "Docker"
                )
        ));

        roleSkillMap.put("Site Reliability Engineer (SRE)", new RoleSkills(
                List.of(
                        "Linux",
                        "Kubernetes",
                        "Docker",
                        "Go",
                        "Python"
                ),
                List.of(
                        "Prometheus",
                        "Grafana",
                        "CI/CD",
                        "Terraform"
                ),
                List.of(
                        "Ansible",
                        "Distributed Systems"
                )
        ));
    }

    public RoleSkills getSkillsForRole(String role) {
        if (role == null || role.isBlank()) {
            return getDefaultSkills();
        }

        return roleSkillMap.entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey().equalsIgnoreCase(role.trim()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(this::getDefaultSkills);
    }

    private RoleSkills getDefaultSkills() {
        return new RoleSkills(
                List.of(
                        "Java",
                        "Software Engineering",
                        "Git",
                        "SQL"
                ),
                List.of(
                        "Problem Solving",
                        "REST API",
                        "Unit Testing"
                ),
                List.of(
                        "Agile",
                        "CI/CD"
                )
        );
    }

    public List<String> getAvailableRoles() {
        return roleSkillMap.keySet()
                .stream()
                .sorted()
                .toList();
    }
}