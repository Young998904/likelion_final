package com.asmanage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 결제 처리 추상화.
 * PortOne 키가 설정돼 있으면 실제 PortOne(V2) 결제 검증을, 없으면 Mock 결제를 사용한다.
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

    @Value("${portone.channel-key:}")
    private String channelKey;

    @Value("${portone.api-secret:}")
    private String apiSecret;

    /** PortOne 실연동 가능 여부(키가 모두 채워졌는지). */
    public boolean isPortOneEnabled() {
        return StringUtils.hasText(storeId) && StringUtils.hasText(channelKey) && StringUtils.hasText(apiSecret);
    }

    public String getStoreId() {
        return storeId;
    }

    public String getChannelKey() {
        return channelKey;
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
     * PortOne 결제 검증 후 확정.
     * paymentId로 PortOne V2 결제 조회 API를 호출해 상태(PAID)와 금액을 대조한 뒤 확정한다.
     */
    public void verifyAndConfirm(Long requestId, String paymentId) {
        int expected = asRequestService.getDetail(requestId).getTotalAmount();

        // 서버에서 직접 PortOne에 결제 내역을 조회(위·변조 방지)
        Map<?, ?> res = RestClient.create().get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + apiSecret)
                .retrieve()
                .body(Map.class);

        String status = res == null ? null : String.valueOf(res.get("status"));
        int paidAmount = extractTotal(res);

        if (!"PAID".equals(status)) {
            throw new IllegalStateException("결제가 완료되지 않았습니다(상태: " + status + ")");
        }
        if (paidAmount != expected) {
            throw new IllegalStateException("결제 금액이 청구 금액과 다릅니다.");
        }
        asRequestService.confirmPayment(requestId, paymentId);
    }

    /** PortOne 응답의 amount.total 추출. */
    private int extractTotal(Map<?, ?> res) {
        if (res != null && res.get("amount") instanceof Map<?, ?> amount
                && amount.get("total") instanceof Number total) {
            return total.intValue();
        }
        return -1;
    }
}
