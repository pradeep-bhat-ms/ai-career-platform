package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class RagQueryResponseDto {
    private String answer;
    private List<RagSourceDto> sources;
}