package com.asmanage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 내부 직원 계정. 로그인 및 담당자 지정의 주체가 된다.
 * 회원가입은 없으며 ADMIN이 계정을 생성한다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디(중복 불가)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // BCrypt로 암호화되어 저장되는 비밀번호
    @Column(nullable = false)
    private String password;

    // 직원 이름
    @Column(nullable = false, length = 50)
    private String name;

    // 권한(ADMIN / STAFF)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // 활성 여부(삭제 대신 비활성화로 사용 중지)
    @Column(nullable = false)
    private boolean active = true;
}
