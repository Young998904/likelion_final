package com.asmanage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A/S를 요청하는 고객.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 고객명(필수)
    @Column(nullable = false, length = 50)
    private String name;

    // 연락처(필수)
    @Column(nullable = false, length = 20)
    private String phone;

    // 우편번호
    @Column(length = 10)
    private String zipcode;

    // 기본 주소
    @Column(length = 255)
    private String address;

    // 상세 주소
    @Column(length = 255)
    private String addressDetail;
}
