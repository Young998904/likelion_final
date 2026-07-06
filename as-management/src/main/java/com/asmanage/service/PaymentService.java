package com.asmanage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 결제 처리 추상화.
 * PortOne 키가 설정돼 있으면 실제 PortOne 결제 검증을, 없으면 Mock 결제를 사용한다.
 * 어느 경로든 결제 성공 시 {@link AsRequestService#confirmPayment(Long, String)}를 호출해
 * 입금대기 → 수리중으로 전이시킨다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AsRequestService asRequestService;

    // 값이 비어 있으면 Mock 결제 사용
    @Value("${portone.store-id:}")
    private String storeId;

    @Value("${portone.api-secret:}")
    private String apiSecret;

    /** PortOne 실연동 가능 여부(키가 모두 채워졌는지). */
    public boolean isPortOneEnabled() {
        return StringUtils.hasText(storeId) && StringUtils.hasText(apiSecret);
    }

    /**
     * Mock 결제. 즉시 성공으로 간주하고 결제를 확정한다.
     * paymentRef는 모의 식별자(MOCK-...)로 남긴다.
     */
    public void payByMock(Long requestId) {
        String mockRef = "MOCK-" + System.currentTimeMillis();
        asRequestService.confirmPayment(requestId, mockRef);
    }

    /**
     * PortOne 결제 검증 후 확정(키 주입 후 활성화).
     * paymentId로 PortOne V2 결제 조회 API를 호출해 상태·금액을 대조한 뒤 확정한다.
     * TODO: api-secret으로 GET https://api.portone.io/payments/{paymentId} 검증 로직 추가.
     */
    public void verifyAndConfirm(Long requestId, String paymentId) {
        // 현재는 키 미설정 상태이므로 사용되지 않음. 검증 성공 시 아래를 호출한다.
        asRequestService.confirmPayment(requestId, paymentId);
    }
}
