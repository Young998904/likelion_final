package com.asmanage.config;

import com.asmanage.domain.*;
import com.asmanage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 최초 실행 시 기본 데이터를 생성한다.
 * 계정 삭제 기능이 없으므로 첫 로그인 보장을 위해 기본 ADMIN 계정을 반드시 시드한다.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final RepairPresetRepository repairPresetRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 이미 계정이 있으면(=최초 실행이 아니면) 시드하지 않는다
        if (employeeRepository.count() > 0) {
            return;
        }

        // 기본 계정 2종: 최고관리자 / 일반 직원
        employeeRepository.save(createEmployee("admin", "admin123", "최고관리자", Role.ADMIN));
        employeeRepository.save(createEmployee("staff", "staff123", "홍길동", Role.STAFF));

        // 샘플 고객
        customerRepository.save(createCustomer("김철수", "010-1111-2222", "06236",
                "서울시 강남구 테헤란로 123", "4층"));
        customerRepository.save(createCustomer("이영희", "010-3333-4444", "13529",
                "경기도 성남시 분당구 판교로 45", "201호"));

        // 샘플 제품
        productRepository.save(createProduct("노트북 X1", "NB-X1-2024", "14인치 모델"));
        productRepository.save(createProduct("무선 이어폰 A2", "EP-A2", "노이즈 캔슬링"));

        // 샘플 수리내역 프리셋
        repairPresetRepository.save(createPreset("액정 교체", 120000));
        repairPresetRepository.save(createPreset("배터리 교체", 55000));
        repairPresetRepository.save(createPreset("기본 점검비", 15000));
    }

    /**
     * 직원 계정 생성(비밀번호는 BCrypt로 암호화).
     */
    private Employee createEmployee(String username, String rawPassword, String name, Role role) {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.setName(name);
        employee.setRole(role);
        employee.setActive(true);
        return employee;
    }

    private Customer createCustomer(String name, String phone, String zipcode,
                                    String address, String addressDetail) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setZipcode(zipcode);
        customer.setAddress(address);
        customer.setAddressDetail(addressDetail);
        return customer;
    }

    private Product createProduct(String name, String modelCode, String note) {
        Product product = new Product();
        product.setName(name);
        product.setModelCode(modelCode);
        product.setNote(note);
        return product;
    }

    private RepairPreset createPreset(String name, int price) {
        RepairPreset preset = new RepairPreset();
        preset.setName(name);
        preset.setPrice(price);
        return preset;
    }
}
