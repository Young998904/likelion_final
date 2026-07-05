package com.asmanage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A/S 대상 제품.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제품명(필수)
    @Column(nullable = false, length = 100)
    private String name;

    // 모델 코드
    @Column(length = 50)
    private String modelCode;

    // 비고
    @Column(length = 255)
    private String note;
}
