package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.entity.Document;
import com.pradeep.aicareerplatform.entity.DocumentChunk;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.DocumentChunkRepository;
import com.pradeep.aicareerplatform.repository.DocumentRepository;
import com.pradeep.aicareerplatform.repository.UserRepository;

import org.springframework.ai.document.Document.Builder;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;

    public DocumentIngestionService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            UserRepository userRepository,
            VectorStore vectorStore) {

        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.userRepository = userRepository;
        this.vectorStore = vectorStore;
    }

    public Document ingestDocument(
            MultipartFile file,
            String title,
            String category,
            String userEmail) throws IOException {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        // 1. Create document record
        Document document = new Document();

        document.setUser(user);
        document.setTitle(title);
        document.setCategory(category);
        document.setFileName(file.getOriginalFilename());
        document.setStatus("PROCESSING");

        documentRepository.save(document);

        try {

            // 2. Read PDF
            PagePdfDocumentReader pdfReader =
                    new PagePdfDocumentReader(
                            new InputStreamResource(file.getInputStream())
                    );

            List<org.springframework.ai.document.Document> rawDocs =
                    pdfReader.get();

            // 3. Split PDF text into chunk
            TokenTextSplitter splitter =
                    TokenTextSplitter.builder().build();

            List<org.springframework.ai.document.Document> splitDocs =
                    splitter.apply(rawDocs);

            // 4. Store chunks + prepare vector documents
            List<org.springframework.ai.document.Document> enrichedDocs =
                    new ArrayList<>();

            int index = 0;

            for (org.springframework.ai.document.Document splitDoc : splitDocs) {

                // Store chunk in PostgreSQL
                DocumentChunk chunk = new DocumentChunk();

                chunk.setDocument(document);
                chunk.setChunkText(splitDoc.getText());
                chunk.setChunkIndex(index);

                documentChunkRepository.save(chunk);

                // Metadata for vector store
                Map<String, Object> metadata =
                        new HashMap<>();

                metadata.put("documentId", document.getId());
                metadata.put("chunkId", chunk.getId());
                metadata.put("title", title);
                metadata.put(
                        "category",
                        category != null ? category : "General"
                );
                metadata.put("userEmail", userEmail);

                // Create Spring AI document
                org.springframework.ai.document.Document enriched =
                        new org.springframework.ai.document.Document(
                                splitDoc.getText(),
                                metadata
                        );

                enrichedDocs.add(enriched);

                index++;
            }

            // 5. Generate embeddings and store in PGVector
            vectorStore.add(enrichedDocs);

            // 6. Mark document as ready
            document.setStatus("READY");
            documentRepository.save(document);

        } catch (Exception e) {

            document.setStatus("FAILED");
            documentRepository.save(document);

            throw e;
        }

        return document;
    }
}