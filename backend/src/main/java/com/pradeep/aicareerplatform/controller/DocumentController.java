package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.DocumentUploadResponseDto;
import com.pradeep.aicareerplatform.entity.Document;
import com.pradeep.aicareerplatform.repository.DocumentChunkRepository;
import com.pradeep.aicareerplatform.service.DocumentIngestionService;
import com.pradeep.aicareerplatform.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentService documentService;


    public DocumentController(DocumentIngestionService documentIngestionService,
                              DocumentChunkRepository documentChunkRepository, DocumentService documentService) {
        this.documentIngestionService = documentIngestionService;
        this.documentChunkRepository = documentChunkRepository;
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponseDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "category", required = false) String category,
            Authentication authentication) throws IOException {

        String userEmail = authentication.getName();
        Document document = documentIngestionService.ingestDocument(file, title, category, userEmail);
        int chunkCount = documentChunkRepository.findByDocumentId(document.getId()).size();

        return ResponseEntity.ok(new DocumentUploadResponseDto(
                document.getId(), document.getTitle(), document.getStatus(), chunkCount, "Document processed successfully"
        ));
    }

    @GetMapping("/my-documents")
    public ResponseEntity<List<Document>> getMyDocuments(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(documentService.getDocumentsForUser(userEmail));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId, Authentication authentication) {
        String userEmail = authentication.getName();
        documentService.deleteDocument(documentId, userEmail);
        return ResponseEntity.noContent().build();
    }
}