package com.asmanage.config;

import com.asmanage.domain.*;
import com.asmanage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * QA용 더미 데이터 시더.
 * 실행 옵션 app.dummy-data=true 일 때만 동작하며(평상시 실행엔 영향 없음),
 * 여러 상태·기간에 걸친 접수 데이터를 생성해 목록 필터·상세·대시보드 차트를 검증할 수 있게 한다.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.dummy-data", havingValue = "true")
@RequiredArgsConstructor
public class DummyDataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final RepairPresetRepository repairPresetRepository;
    private final AsRequestRepository asRequestRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final PasswordEncoder passwordEncoder;

    // 재현 가능하도록 고정 시드 사용
    private final Random random = new Random(42);

    @Override
    @Transactional
    public void run(String... args) {
        // 접수 데이터가 이미 있으면(더미가 이미 로드됨) 생략
        if (asRequestRepository.count() > 0) {
            return;
        }

        List<Employee> assignees = seedEmployees();
        List<Customer> customers = seedCustomers();
        List<Product> products = seedProducts();
        List<RepairPreset> presets = seedPresets();

        seedRequests(customers, products, presets, assignees);
    }

    /** 담당자 후보 STAFF 3명 추가 후 활성 직원 전체 반환. */
    private List<Employee> seedEmployees() {
        String[][] staff = {{"park1", "박기사"}, {"lee1", "이수리"}, {"jung1", "정담당"}};
        for (String[] s : staff) {
            if (!employeeRepository.existsByUsername(s[0])) {
                Employee e = new Employee();
                e.setUsername(s[0]);
                e.setName(s[1]);
                e.setPassword(passwordEncoder.encode("pw12345"));
                e.setRole(Role.STAFF);
                e.setActive(true);
                employeeRepository.save(e);
            }
        }
        return employeeRepository.findByActiveTrueOrderByNameAsc();
    }

    /** 더미 고객 10명. */
    private List<Customer> seedCustomers() {
        String[] surnames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"};
        String[] givenNames = {"민준", "서연", "도윤", "지우", "하준", "서윤", "예준", "지호", "수아", "지훈"};
        String[][] addresses = {
                {"06236", "서울시 강남구 테헤란로 152", "3층"},
                {"04524", "서울시 중구 세종대로 110", "502호"},
                {"13529", "경기도 성남시 분당구 판교역로 235", "A동 801호"},
                {"03187", "서울시 종로구 종로 1", "7층"},
                {"48058", "부산시 해운대구 센텀중앙로 90", "1203호"}
        };
        List<Customer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Customer c = new Customer();
            c.setName(surnames[i % surnames.length] + givenNames[i % givenNames.length]);
            c.setPhone(String.format("010-%04d-%04d", random.nextInt(10000), random.nextInt(10000)));
            String[] addr = addresses[i % addresses.length];
            c.setZipcode(addr[0]);
            c.setAddress(addr[1]);
            c.setAddressDetail(addr[2]);
            list.add(customerRepository.save(c));
        }
        return list;
    }

    /** 더미 제품 6종. */
    private List<Product> seedProducts() {
        String[][] products = {
                {"노트북 프로 15", "NB-PRO-15"}, {"무선 이어폰 버즈", "EP-BUZZ"},
                {"스마트폰 갤럭시 S", "SP-GLXY-S"}, {"태블릿 탭 A", "TB-TAB-A"},
                {"블루투스 스피커 미니", "BT-SPK-M"}, {"스마트워치 핏", "SW-FIT"}
        };
        List<Product> list = new ArrayList<>();
        for (String[] p : products) {
            Product prod = new Product();
            prod.setName(p[0]);
            prod.setModelCode(p[1]);
            prod.setNote("QA 더미");
            list.add(productRepository.save(prod));
        }
        return list;
    }

    /** 프리셋 4종 추가 후 전체 반환. */
    private List<RepairPreset> seedPresets() {
        int[][] presets = {{250000}, {80000}, {40000}, {30000}};
        String[] names = {"메인보드 수리", "카메라 모듈 교체", "충전 포트 교체", "스피커 교체"};
        for (int i = 0; i < names.length; i++) {
            RepairPreset preset = new RepairPreset();
            preset.setName(names[i]);
            preset.setPrice(presets[i][0]);
            repairPresetRepository.save(preset);
        }
        return repairPresetRepository.findAll();
    }

    /** 상태·기간이 분산된 접수 30건 생성. */
    private void seedRequests(List<Customer> customers, List<Product> products,
                              List<RepairPreset> presets, List<Employee> assignees) {
        List<AsStatus> plan = buildStatusPlan();
        String[] complaints = {"전원이 안 켜져요", "화면에 줄이 가요", "소리가 안 나요", "충전이 안 됩니다",
                "버튼이 눌리지 않아요", "과열됩니다", "블루투스 연결이 안 돼요", "배터리가 빨리 닳아요"};
        String[] couriers = {"CJ대한통운", "한진택배", "롯데택배", "우체국택배"};

        for (AsStatus status : plan) {
            Customer customer = pick(customers);
            Product product = pick(products);
            LocalDateTime createdAt = LocalDateTime.now()
                    .minusDays(random.nextInt(170)).minusHours(random.nextInt(24));

            AsRequest request = new AsRequest();
            request.setCustomer(customer);
            request.setProduct(product);
            request.setRequestContent(pick(complaints));
            request.setDeliveryZipcode(customer.getZipcode());
            request.setDeliveryAddress(customer.getAddress());
            request.setDeliveryAddressDetail(customer.getAddressDetail());
            request.setStatus(status);
            request.setCreatedAt(createdAt);

            // 접수 상태 외에는 담당자 지정(접수는 절반 정도만)
            if (status != AsStatus.RECEIVED || random.nextBoolean()) {
                request.setAssignee(pick(assignees));
            }

            // 수리내역: 접수는 0~1개, 그 외 1~3개
            int itemCount = (status == AsStatus.RECEIVED) ? random.nextInt(2) : 1 + random.nextInt(3);
            int total = 0;
            for (int k = 0; k < itemCount; k++) {
                RepairPreset preset = pick(presets);
                AsRepairItem item = new AsRepairItem();
                item.setRequest(request);
                item.setPreset(preset);
                item.setName(preset.getName());
                item.setPrice(preset.getPrice());
                request.getRepairItems().add(item);
                total += preset.getPrice();
            }
            request.setTotalAmount(total);

            // 선불 모델: 수리중 이상이면 이미 결제된 상태
            boolean paid = status.ordinal() >= AsStatus.REPAIRING.ordinal();
            boolean reachedDelivery = status == AsStatus.AWAITING_DELIVERY || status == AsStatus.COMPLETED;
            request.setPaid(paid);
            if (paid) {
                request.setPaidAt(createdAt.plusDays(1));
            }
            if (reachedDelivery) {
                request.setCourier(pick(couriers));
                request.setTrackingNo(String.valueOf(100000000L + random.nextInt(900000000)));
            }
            if (status == AsStatus.COMPLETED) {
                LocalDateTime completedAt = createdAt.plusDays(1 + random.nextInt(20));
                if (completedAt.isAfter(LocalDateTime.now())) {
                    completedAt = LocalDateTime.now();
                }
                request.setCompletedAt(completedAt);
            }

            asRequestRepository.save(request);

            // 알림 로그(도달한 단계까지): 접수 → 비용청구 → 결제완료 → 배송 → 완료
            addNotification(request, NotificationType.RECEIPT,
                    "접수가 등록되었습니다. (고객: " + customer.getName() + ", 제품: " + product.getName() + ")", createdAt);
            if (status.ordinal() >= AsStatus.AWAITING_PAYMENT.ordinal()) {
                addNotification(request, NotificationType.PAYMENT_REQUEST,
                        "비용이 청구되었습니다. 청구금액 " + total + "원", createdAt.plusDays(1));
            }
            if (paid) {
                addNotification(request, NotificationType.PAYMENT_DONE,
                        "결제가 완료되었습니다. 수리를 시작합니다.", createdAt.plusDays(1));
            }
            if (reachedDelivery) {
                LocalDateTime deliveredAt = request.getCompletedAt() != null
                        ? request.getCompletedAt() : createdAt.plusDays(2);
                addNotification(request, NotificationType.DELIVERY,
                        "배송이 시작되었습니다. 송장번호 " + request.getTrackingNo(), deliveredAt);
            }
            if (status == AsStatus.COMPLETED) {
                addNotification(request, NotificationType.COMPLETE,
                        "A/S 처리가 완료되었습니다.", request.getCompletedAt());
            }
        }
    }

    /** 상태 분포 계획(총 30건) 생성 후 섞기. */
    private List<AsStatus> buildStatusPlan() {
        List<AsStatus> plan = new ArrayList<>();
        addRepeated(plan, AsStatus.RECEIVED, 5);
        addRepeated(plan, AsStatus.REPAIRING, 5);
        addRepeated(plan, AsStatus.AWAITING_PAYMENT, 4);
        addRepeated(plan, AsStatus.AWAITING_DELIVERY, 4);
        addRepeated(plan, AsStatus.COMPLETED, 12);
        Collections.shuffle(plan, random);
        return plan;
    }

    private void addRepeated(List<AsStatus> plan, AsStatus status, int count) {
        for (int i = 0; i < count; i++) {
            plan.add(status);
        }
    }

    private void addNotification(AsRequest request, NotificationType type, String message, LocalDateTime at) {
        NotificationLog log = new NotificationLog();
        log.setRequest(request);
        log.setType(type);
        log.setMessage(message);
        log.setCreatedAt(at);
        notificationLogRepository.save(log);
    }

    private <T> T pick(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private String pick(String[] array) {
        return array[random.nextInt(array.length)];
    }
}
