package com.asmanage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 수리내역 프리셋 등록/수정 폼.
 */
@Getter
@Setter
public class RepairPresetForm {

    private Long id;

    @NotBlank(message = "수리내역 명칭을 입력하세요.")
    private String name;

    @NotNull(message = "금액을 입력하세요.")
    @Min(value = 0, message = "금액은 0 이상이어야 합니다.")
    private Integer price;
}
