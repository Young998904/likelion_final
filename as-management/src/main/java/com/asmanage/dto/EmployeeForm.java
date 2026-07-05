package com.asmanage.dto;

import com.asmanage.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 직원 계정 등록/수정 폼.
 * 비밀번호는 신규 등록 시 필수, 수정 시 비워두면 기존 값을 유지한다.
 */
@Getter
@Setter
public class EmployeeForm {

    private Long id;

    @NotBlank(message = "아이디를 입력하세요.")
    private String username;

    @NotBlank(message = "이름을 입력하세요.")
    private String name;

    // 신규: 초기 비밀번호 / 수정: 비우면 유지, 입력하면 재설정
    private String password;

    @NotNull(message = "권한을 선택하세요.")
    private Role role;

    private boolean active = true;
}
