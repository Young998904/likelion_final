package com.asmanage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A/S 접수 건. 시스템의 핵심 처리 대상이며 상태(status)를 따라 진행된다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AsRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 고객(필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // 제품(필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    // 담당자(미지정 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Employee assignee;

    // 진행 상태(초기값: 접수)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AsStatus status = AsStatus.RECEIVED;

    // 고객 요청 내용
    @Column(columnDefinition = "TEXT")
    private String requestContent;

    // 배송지 우편번호(기본값은 고객 주소에서 복사, 수정 가능)
    @Column(length = 10)
    private String deliveryZipcode;

    // 배송지 주소
    @Column(length = 255)
    private String deliveryAddress;

    // 배송지 상세주소
    @Column(length = 255)
    private String deliveryAddressDetail;

    // 송장번호(완료의 필수 조건은 아님)
    @Column(length = 50)
    private String trackingNo;

    // 택배사
    @Column(length = 50)
    private String courier;

    // 수리내역 합계 금액(수리비만, 제품가/부가세 미포함)
    @Column(nullable = false)
    private int totalAmount = 0;

    // 입금 완료 여부
    @Column(nullable = false)
    private boolean paid = false;

    // 접수 일시
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 완료 처리 일시
    private LocalDateTime completedAt;

    // 수리내역 라인 목록
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AsRepairItem> repairItems = new ArrayList<>();

    /**
     * 신규 저장 직전 접수 일시를 자동 기록한다.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
