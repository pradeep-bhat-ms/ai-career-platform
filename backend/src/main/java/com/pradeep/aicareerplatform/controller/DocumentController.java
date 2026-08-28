package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.DocumentUploadResponseDto;
import com.pradeep.aicareerplatform.entity.Document;
import com.pradeep.aicareerplatform.repository.DocumentChunkRepository;
import com.pradeep.aicareerplatform.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentController(DocumentIngestionService documentIngestionService,
                              DocumentChunkRepository documentChunkRepository) {
        this.documentIngestionService = documentIngestionService;
        this.documentChunkRepository = documentChunkRepository;
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
}