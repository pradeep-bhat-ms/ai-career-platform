package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.RagQueryResponseDto;
import com.pradeep.aicareerplatform.dto.RagSourceDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagQueryService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    private static final String NO_ANSWER_MESSAGE =
            "I don't have enough information in your uploaded documents to answer this.";

    public RagQueryService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public RagQueryResponseDto query(String question, String category, String userEmail) {
        FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression;

        if (category != null && !category.isBlank()) {
            filterExpression = filterBuilder.and(
                    filterBuilder.eq("userEmail", userEmail),
                    filterBuilder.eq("category", category)
            ).build();
        } else {
            filterExpression = filterBuilder.eq("userEmail", userEmail).build();
        }

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(4)
                .filterExpression(filterExpression)
                .build();

        List<Document> relevantChunks = vectorStore.similaritySearch(searchRequest);

        if (relevantChunks == null || relevantChunks.isEmpty()) {
            return new RagQueryResponseDto(NO_ANSWER_MESSAGE, List.of());
        }

        String context = relevantChunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String promptText = """
                You are a helpful assistant answering questions based ONLY on the context below.

                Context from the user's uploaded documents:
                %s

                Question: %s

                Rules:
                - Answer ONLY using information from the context above.
                - If the context does not contain enough information to answer, respond EXACTLY with:
                  "%s"
                - Do not use any outside knowledge.
                - Keep the answer clear and concise.
                """.formatted(context, question, NO_ANSWER_MESSAGE);

        String answer = chatClient.prompt()
                .user(promptText)
                .call()
                .content();

        List<RagSourceDto> sources = relevantChunks.stream()
                .map(doc -> new RagSourceDto(
                        (String) doc.getMetadata().getOrDefault("title", "Unknown document"),
                        doc.getText().length() > 150 ? doc.getText().substring(0, 150) + "..." : doc.getText()
                ))
                .distinct()
                .collect(Collectors.toList());

        return new RagQueryResponseDto(answer, sources);
    }
}