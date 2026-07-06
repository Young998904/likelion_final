package com.asmanage.domain;

import lombok.Getter;

/**
 * 알림 로그 유형(실제 SMS 대신 화면/DB에 기록하는 통지 이력).
 */
@Getter
public enum NotificationType {
    RECEIPT("접수"),
    PAYMENT_REQUEST("비용청구"),
    PAYMENT_DONE("결제완료"),
    DELIVERY("배송"),
    COMPLETE("완료");

    // 화면에 표시할 한글 유형명
    private final String label;

    NotificationType(String label) {
        this.label = label;
    }
}
