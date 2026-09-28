package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.entity.Document;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.DocumentChunkRepository;
import com.pradeep.aicareerplatform.repository.DocumentRepository;
import com.pradeep.aicareerplatform.repository.UserRepository;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentChunkRepository documentChunkRepository,
                           UserRepository userRepository,
                           VectorStore vectorStore) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.userRepository = userRepository;
        this.vectorStore = vectorStore;
    }

    public List<Document> getDocumentsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return documentRepository.findByUserId(user.getId());
    }

    public void deleteDocument(Long documentId, String userEmail) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!document.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this document");
        }

        FilterExpressionBuilder builder = new FilterExpressionBuilder();

        Filter.Expression filter = builder
                .eq("documentId", documentId)
                .build();

        vectorStore.delete(filter);

        documentChunkRepository.deleteAll(
                documentChunkRepository.findByDocumentId(documentId)
        );

        documentRepository.delete(document);
    }
}