package com.asmanage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 자주 쓰는 수리내역 프리셋(명칭 + 금액).
 * 접수 처리 시 선택하여 수리내역으로 추가한다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RepairPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수리내역 명칭
    @Column(nullable = false, length = 100)
    private String name;

    // 금액(원)
    @Column(nullable = false)
    private int price;
}
