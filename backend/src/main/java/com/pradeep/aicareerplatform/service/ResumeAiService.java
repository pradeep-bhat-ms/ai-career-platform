package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.ResumeExtractionDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ResumeAiService {

    private final ChatClient chatClient;

    public ResumeAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ResumeExtractionDto extractResumeData(String resumeText) {

        String promptText = """
                You are an expert resume parser.

                Extract structured information from the resume text below.

                IMPORTANT RULES:
                1. Extract only information explicitly present in the resume.
                2. Never invent skills, experience, responsibilities, metrics, dates, technologies, companies, URLs, or achievements.
                3. Preserve the meaning of the original resume.
                4. Do not infer information that is not explicitly stated.
                5. Extract every project separately.
                6. For every project, extract the project name, technology stack, objective, description/responsibilities, GitHub information, and live demo information when explicitly available.
                7. If a project field is not present, return an empty string instead of inventing information.
                8. Keep technical technologies exactly as supported by the resume.
                9. Preserve internship/training information as experience evidence.
                10. The projects field must contain structured project objects, not plain project names.

                PROJECT EXTRACTION:

                For each project identify:
                - name
                - techStack
                - objective
                - description
                - github
                - liveDemo

                Combine the project's explicitly stated responsibilities and implementation details into the description field.

                Resume text:
                %s
                """.formatted(resumeText);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(ResumeExtractionDto.class);
    }

    public byte[] generatePdfFromText(String content) throws IOException {

        if (content == null || content.isBlank()) {
            content = "No content available to generate PDF.";
        }

        content = content.replace("•", "- ")
                .replace("—", "-")
                .replace("–", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("’", "'")
                .replace("‘", "'");

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream currentStream =
                    new PDPageContentStream(document, page);

            currentStream.setFont(
                    PDType1Font.HELVETICA,
                    10
            );

            currentStream.beginText();
            currentStream.setLeading(13.5f);
            currentStream.newLineAtOffset(40, 750);

            int lineCount = 0;
            String[] lines = content.split("\n");

            for (String line : lines) {

                String cleanLine =
                        line.replaceAll(
                                "[^\\x20-\\x7F]",
                                ""
                        ).trim();

                if (cleanLine.isEmpty()) {
                    currentStream.newLine();
                    lineCount++;
                    continue;
                }

                while (cleanLine.length() > 85) {

                    String subLine =
                            cleanLine.substring(0, 85);

                    int lastSpace =
                            subLine.lastIndexOf(' ');

                    if (lastSpace > 20) {
                        subLine =
                                cleanLine.substring(
                                        0,
                                        lastSpace
                                );
                    }

                    currentStream.showText(subLine);
                    currentStream.newLine();

                    lineCount++;

                    cleanLine =
                            cleanLine.substring(
                                    subLine.length()
                            ).trim();

                    if (lineCount > 48) {

                        currentStream.endText();
                        currentStream.close();

                        page = new PDPage();
                        document.addPage(page);

                        currentStream =
                                new PDPageContentStream(
                                        document,
                                        page
                                );

                        currentStream.setFont(
                                PDType1Font.HELVETICA,
                                10
                        );

                        currentStream.beginText();
                        currentStream.setLeading(13.5f);
                        currentStream.newLineAtOffset(
                                40,
                                750
                        );

                        lineCount = 0;
                    }
                }

                currentStream.showText(cleanLine);
                currentStream.newLine();

                lineCount++;

                if (lineCount > 48) {

                    currentStream.endText();
                    currentStream.close();

                    page = new PDPage();
                    document.addPage(page);

                    currentStream =
                            new PDPageContentStream(
                                    document,
                                    page
                            );

                    currentStream.setFont(
                            PDType1Font.HELVETICA,
                            10
                    );

                    currentStream.beginText();
                    currentStream.setLeading(13.5f);
                    currentStream.newLineAtOffset(
                            40,
                            750
                    );

                    lineCount = 0;
                }
            }

            currentStream.endText();
            currentStream.close();

            document.save(out);

            return out.toByteArray();
        }
    }

    public String getExperienceCapMessage(
            double experienceScore) {

        if (experienceScore < 60) {
            return String.format("""
                    Experience score: %.0f%%

                    The analyzer evaluates the experience evidence explicitly present in the resume.

                    Legitimate ways to strengthen this section:
                    1. Clearly structure internships, trainee roles, or relevant practical experience.
                    2. Highlight responsibilities and technologies actually used.
                    3. Include measurable achievements only when they are supported by your actual experience.
                    4. Align experience descriptions with the target role without adding unsupported claims.
                    """, experienceScore);
        }

        return "Experience score meets the current target criteria.";
    }
}