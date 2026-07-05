package com.asmanage.dto;

/**
 * 제품 자동완성 응답 항목(JSON).
 */
public record ProductSuggestion(
        Long id,
        String name,
        String modelCode
) {
}
