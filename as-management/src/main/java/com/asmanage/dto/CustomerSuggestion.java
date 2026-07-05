package com.asmanage.dto;

/**
 * 고객 자동완성 응답 항목(JSON).
 */
public record CustomerSuggestion(
        Long id,
        String name,
        String phone,
        String zipcode,
        String address,
        String addressDetail
) {
}
