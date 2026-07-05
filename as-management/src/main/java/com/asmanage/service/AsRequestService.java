package com.asmanage.service;

import com.asmanage.domain.*;
import com.asmanage.dto.AsRequestForm;
import com.asmanage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A/S 접수 라이프사이클 비즈니스 로직.
 * 상태는 접수 → 수리중 → 입금대기 → 배송대기 → 완료로 순방향으로만 진행한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsRequestService {

    private final AsRequestRepository asRequestRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final RepairPresetRepository repairPresetRepository;
    private final NotificationLogRepository notificationLogRepository;

    /** 접수 목록. 상태/고객명/담당자 조건으로 필터(값이 없으면 전체). */
    public List<AsRequest> list(AsStatus status, String customerKeyword, Long assigneeId) {
        return asRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> !StringUtils.hasText(customerKeyword)
                        || r.getCustomer().getName().contains(customerKeyword))
                .filter(r -> assigneeId == null
                        || (r.getAssignee() != null && r.getAssignee().getId().equals(assigneeId)))
                .toList();
    }

    /** 접수 상세 조회. */
    public AsRequest getDetail(Long id) {
        return findById(id);
    }

    /** 특정 접수의 알림 로그(최신순). */
    public List<NotificationLog> getNotifications(Long requestId) {
        return notificationLogRepository.findByRequestIdOrderByCreatedAtDesc(requestId);
    }

    /**
     * 신규 접수 등록. 고객/제품은 기존 선택 또는 신규 인라인 등록.
     * 초기 상태 = 접수. 접수 알림 로그를 기록한다.
     */
    @Transactional
    public Long create(AsRequestForm form) {
        Customer customer = resolveCustomer(form);
        Product product = resolveProduct(form);

        AsRequest request = new AsRequest();
        request.setCustomer(customer);
        request.setProduct(product);
        request.setStatus(AsStatus.RECEIVED);
        request.setRequestContent(form.getRequestContent());

        // 배송지: 입력값이 없으면 고객 주소를 기본값으로 사용
        request.setDeliveryZipcode(defaultText(form.getDeliveryZipcode(), customer.getZipcode()));
        request.setDeliveryAddress(defaultText(form.getDeliveryAddress(), customer.getAddress()));
        request.setDeliveryAddressDetail(defaultText(form.getDeliveryAddressDetail(), customer.getAddressDetail()));

        asRequestRepository.save(request);

        writeNotification(request, NotificationType.RECEIPT,
                "접수가 등록되었습니다. (고객: " + customer.getName() + ", 제품: " + product.getName() + ")");
        return request.getId();
    }

    /** 담당자 지정/변경. */
    @Transactional
    public void assign(Long id, Long employeeId) {
        AsRequest request = findById(id);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("담당자를 찾을 수 없습니다."));
        request.setAssignee(employee);
    }

    /** 프리셋에서 수리내역 추가. */
    @Transactional
    public void addPresetItem(Long id, Long presetId) {
        AsRequest request = getEditable(id);
        RepairPreset preset = repairPresetRepository.findById(presetId)
                .orElseThrow(() -> new IllegalArgumentException("프리셋을 찾을 수 없습니다."));
        AsRepairItem item = new AsRepairItem();
        item.setRequest(request);
        item.setPreset(preset);
        item.setName(preset.getName());
        item.setPrice(preset.getPrice());
        request.getRepairItems().add(item);
        recalcTotal(request);
    }

    /** 수동으로 수리내역 추가. */
    @Transactional
    public void addManualItem(Long id, String name, int price) {
        AsRequest request = getEditable(id);
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("수리내역 명칭을 입력하세요.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다.");
        }
        AsRepairItem item = new AsRepairItem();
        item.setRequest(request);
        item.setName(name);
        item.setPrice(price);
        request.getRepairItems().add(item);
        recalcTotal(request);
    }

    /** 수리내역 삭제. */
    @Transactional
    public void removeItem(Long id, Long itemId) {
        AsRequest request = getEditable(id);
        request.getRepairItems().removeIf(item -> item.getId().equals(itemId));
        recalcTotal(request);
    }

    /** 접수 → 수리중. 담당자가 지정되어 있어야 한다. */
    @Transactional
    public void startRepair(Long id) {
        AsRequest request = findById(id);
        requireStatus(request, AsStatus.RECEIVED);
        if (request.getAssignee() == null) {
            throw new IllegalStateException("담당자를 먼저 지정하세요.");
        }
        request.setStatus(AsStatus.REPAIRING);
    }

    /** 수리중 → 입금대기. 수리내역이 1건 이상 있어야 하며 입금요청 알림을 기록한다. */
    @Transactional
    public void requestPayment(Long id) {
        AsRequest request = findById(id);
        requireStatus(request, AsStatus.REPAIRING);
        if (request.getRepairItems().isEmpty()) {
            throw new IllegalStateException("수리내역을 1건 이상 입력하세요.");
        }
        request.setStatus(AsStatus.AWAITING_PAYMENT);
        writeNotification(request, NotificationType.PAYMENT_REQUEST,
                "입금 요청되었습니다. 청구금액 " + request.getTotalAmount() + "원");
    }

    /** 입금대기 → 배송대기. paid=true 설정과 상태 전이를 한 트랜잭션으로 처리. */
    @Transactional
    public void confirmPayment(Long id) {
        AsRequest request = findById(id);
        requireStatus(request, AsStatus.AWAITING_PAYMENT);
        request.setPaid(true);
        request.setStatus(AsStatus.AWAITING_DELIVERY);
    }

    /** 송장 정보 입력(배송대기 상태). 배송 알림을 기록한다. */
    @Transactional
    public void saveTracking(Long id, String courier, String trackingNo) {
        AsRequest request = findById(id);
        requireStatus(request, AsStatus.AWAITING_DELIVERY);
        if (!StringUtils.hasText(trackingNo)) {
            throw new IllegalStateException("송장번호를 입력하세요.");
        }
        request.setCourier(courier);
        request.setTrackingNo(trackingNo);
        writeNotification(request, NotificationType.DELIVERY,
                "배송이 시작되었습니다. 송장번호 " + trackingNo);
    }

    /** 배송대기 → 완료. 송장은 필수 조건이 아니다. */
    @Transactional
    public void complete(Long id) {
        AsRequest request = findById(id);
        requireStatus(request, AsStatus.AWAITING_DELIVERY);
        request.setStatus(AsStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
    }

    // ===== 내부 헬퍼 =====

    private AsRequest findById(Long id) {
        return asRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("접수 건을 찾을 수 없습니다."));
    }

    /** 수리내역 수정이 허용되는 상태(접수/수리중)인지 확인 후 반환. */
    private AsRequest getEditable(Long id) {
        AsRequest request = findById(id);
        if (request.getStatus() != AsStatus.RECEIVED && request.getStatus() != AsStatus.REPAIRING) {
            throw new IllegalStateException("입금 요청 이후에는 수리내역을 변경할 수 없습니다.");
        }
        return request;
    }

    private void requireStatus(AsRequest request, AsStatus expected) {
        if (request.getStatus() != expected) {
            throw new IllegalStateException("현재 상태(" + request.getStatus().getLabel() + ")에서는 처리할 수 없습니다.");
        }
    }

    private void recalcTotal(AsRequest request) {
        int total = request.getRepairItems().stream().mapToInt(AsRepairItem::getPrice).sum();
        request.setTotalAmount(total);
    }

    private Customer resolveCustomer(AsRequestForm form) {
        if (form.getCustomerId() != null) {
            return customerRepository.findById(form.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 고객을 찾을 수 없습니다."));
        }
        if (!StringUtils.hasText(form.getNewCustomerName()) || !StringUtils.hasText(form.getNewCustomerPhone())) {
            throw new IllegalArgumentException("고객을 선택하거나 신규 고객의 이름과 연락처를 입력하세요.");
        }
        Customer customer = new Customer();
        customer.setName(form.getNewCustomerName());
        customer.setPhone(form.getNewCustomerPhone());
        customer.setZipcode(form.getNewCustomerZipcode());
        customer.setAddress(form.getNewCustomerAddress());
        customer.setAddressDetail(form.getNewCustomerAddressDetail());
        return customerRepository.save(customer);
    }

    private Product resolveProduct(AsRequestForm form) {
        if (form.getProductId() != null) {
            return productRepository.findById(form.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 제품을 찾을 수 없습니다."));
        }
        if (!StringUtils.hasText(form.getNewProductName())) {
            throw new IllegalArgumentException("제품을 선택하거나 신규 제품명을 입력하세요.");
        }
        Product product = new Product();
        product.setName(form.getNewProductName());
        product.setModelCode(form.getNewProductModelCode());
        return productRepository.save(product);
    }

    private void writeNotification(AsRequest request, NotificationType type, String message) {
        NotificationLog log = new NotificationLog();
        log.setRequest(request);
        log.setType(type);
        log.setMessage(message);
        notificationLogRepository.save(log);
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
