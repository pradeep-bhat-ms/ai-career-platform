package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScoreCategoryDto {
    private String category;      // e.g. "Skills Match"
    private int score;            // 0-100
    private String explanation;   // why this category scored what it did
}