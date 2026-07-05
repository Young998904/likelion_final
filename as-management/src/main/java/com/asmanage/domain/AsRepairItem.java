package com.asmanage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A/S 접수 건에 포함된 개별 수리내역 라인.
 * 프리셋에서 선택하거나(preset 참조) 수동으로 입력할 수 있다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AsRepairItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 접수 건
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id")
    private AsRequest request;

    // 원본 프리셋(수동 입력 항목이면 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_id")
    private RepairPreset preset;

    // 수리내역 명칭(저장 시점의 값을 보존)
    @Column(nullable = false, length = 100)
    private String name;

    // 금액(원)
    @Column(nullable = false)
    private int price;
}
