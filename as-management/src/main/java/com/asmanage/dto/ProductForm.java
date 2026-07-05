package com.asmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 제품 등록/수정 폼.
 */
@Getter
@Setter
public class ProductForm {

    private Long id;

    @NotBlank(message = "제품명을 입력하세요.")
    private String name;

    private String modelCode;

    private String note;
}
