package com.asmanage.domain;

import lombok.Getter;

/**
 * A/S 접수 건의 진행 상태.
 * 접수 → 입금대기 → 수리중 → 배송대기 → 완료 순으로만 진행한다(역방향 없음).
 * (선불 모델: 견적·청구 후 결제가 확인되어야 수리를 시작한다)
 */
@Getter
public enum AsStatus {
    RECEIVED("접수"),
    AWAITING_PAYMENT("입금대기"),
    REPAIRING("수리중"),
    AWAITING_DELIVERY("배송대기"),
    COMPLETED("완료");

    // 화면에 표시할 한글 상태명
    private final String label;

    AsStatus(String label) {
        this.label = label;
    }
}
