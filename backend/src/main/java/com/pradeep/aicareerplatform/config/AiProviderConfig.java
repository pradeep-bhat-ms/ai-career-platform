package com.pradeep.aicareerplatform.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class AiProviderConfig {

    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder(
            @Qualifier("openAiChatModel") ChatModel groqModel) {

        return ChatClient.builder(groqModel);
    }

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(
            @Qualifier("googleGenAiTextEmbedding") EmbeddingModel geminiEmbedding) {

        return geminiEmbedding;
    }
}