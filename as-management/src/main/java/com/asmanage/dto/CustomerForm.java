package com.asmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 고객 등록/수정 폼. 엔티티를 화면에 직접 노출하지 않기 위한 DTO.
 */
@Getter
@Setter
public class CustomerForm {

    // 신규는 null, 수정은 기존 고객 id
    private Long id;

    @NotBlank(message = "고객명을 입력하세요.")
    private String name;

    @NotBlank(message = "연락처를 입력하세요.")
    @Pattern(regexp = "^[0-9-]{9,20}$", message = "연락처는 숫자와 '-'만, 9~20자로 입력하세요.")
    private String phone;

    private String zipcode;

    private String address;

    private String addressDetail;
}
