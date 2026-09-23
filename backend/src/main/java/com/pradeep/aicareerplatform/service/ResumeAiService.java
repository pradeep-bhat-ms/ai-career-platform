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
                You are an expert resume parser. Extract structured information from the resume text below.
                Only extract information that is explicitly present in the text. Do not invent or assume anything.

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

        // Clean common unicode symbols that standard PDFBox Helvetica cannot render
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

            PDPageContentStream currentStream = new PDPageContentStream(document, page);
            // Fix: Use PDType1Font.HELVETICA directly for PDFBox 3.x
            currentStream.setFont(PDType1Font.HELVETICA, 10);
            currentStream.beginText();
            currentStream.setLeading(13.5f);
            currentStream.newLineAtOffset(40, 750);

            int lineCount = 0;
            String[] lines = content.split("\n");

            for (String line : lines) {
                String cleanLine = line.replaceAll("[^\\x20-\\x7F]", "").trim();

                if (cleanLine.isEmpty()) {
                    currentStream.newLine();
                    lineCount++;
                    continue;
                }

                while (cleanLine.length() > 85) {
                    String subLine = cleanLine.substring(0, 85);
                    int lastSpace = subLine.lastIndexOf(' ');

                    if (lastSpace > 20) {
                        subLine = cleanLine.substring(0, lastSpace);
                    }

                    currentStream.showText(subLine);
                    currentStream.newLine();
                    lineCount++;

                    cleanLine = cleanLine.substring(subLine.length()).trim();

                    if (lineCount > 48) {
                        currentStream.endText();
                        currentStream.close();

                        page = new PDPage();
                        document.addPage(page);

                        currentStream = new PDPageContentStream(document, page);
                        currentStream.setFont(PDType1Font.HELVETICA, 10);
                        currentStream.beginText();
                        currentStream.setLeading(13.5f);
                        currentStream.newLineAtOffset(40, 750);
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

                    currentStream = new PDPageContentStream(document, page);
                    currentStream.setFont(PDType1Font.HELVETICA, 10);
                    currentStream.beginText();
                    currentStream.setLeading(13.5f);
                    currentStream.newLineAtOffset(40, 750);
                    lineCount = 0;
                }
            }

            currentStream.endText();
            currentStream.close();

            document.save(out);
            return out.toByteArray();
        }
    }

    public String getExperienceCapMessage(double experienceScore) {
        if (experienceScore < 60) {
            return String.format("""
                Note on ATS Score Cap (Experience: %.0f%%):
                The AI optimizer improves formatting, keywords, and project impacts, but cannot fabricate missing years of formal industry work experience.
                
                How to legitimately increase Experience score:
                1. Reformat structured Internships, Trainee roles, or Freelance projects as formal experience entries.
                2. Highlight measurable achievements and metrics (e.g., performance tuning %%, users served).
                3. Align job title terminology closely with target role descriptions.
                """, experienceScore);
        }
        return "Experience score meets target criteria.";
    }
}