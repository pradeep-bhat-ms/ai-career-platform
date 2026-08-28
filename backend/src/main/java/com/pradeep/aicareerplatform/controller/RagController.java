package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.RagQueryRequestDto;
import com.pradeep.aicareerplatform.dto.RagQueryResponseDto;
import com.pradeep.aicareerplatform.service.RagQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagQueryService ragQueryService;

    public RagController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @PostMapping("/query")
    public ResponseEntity<RagQueryResponseDto> query(
            @Valid @RequestBody RagQueryRequestDto request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        RagQueryResponseDto response = ragQueryService.query(request.getQuestion(), request.getCategory(), userEmail);
        return ResponseEntity.ok(response);
    }
}