package com.asmanage.service;

import com.asmanage.domain.Customer;
import com.asmanage.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 고객 포털 인증(회원가입/로그인) 비즈니스 로직.
 * 식별키는 전화번호이며 4자리 PIN(BCrypt)으로 인증한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerPortalService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 최초 회원가입. 이름·전화·PIN·배송지를 등록하고 생성된 고객 id를 반환한다.
     * 동일 전화번호가 이미 있으면 가입을 막는다.
     */
    @Transactional
    public Long signup(String name, String phone, String pin,
                       String zipcode, String address, String addressDetail) {
        validatePin(pin);
        if (!StringUtils.hasText(name) || !StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("이름과 전화번호를 입력하세요.");
        }
        if (customerRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다. 로그인해 주세요.");
        }
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setPin(passwordEncoder.encode(pin));
        customer.setZipcode(zipcode);
        customer.setAddress(address);
        customer.setAddressDetail(addressDetail);
        customerRepository.save(customer);
        return customer.getId();
    }

    /**
     * 로그인. 전화번호+PIN 검증 후 고객 id를 반환한다.
     */
    public Long login(String phone, String pin) {
        Customer customer = customerRepository.findFirstByPhoneOrderByIdDesc(phone)
                .filter(c -> StringUtils.hasText(c.getPin()))
                .filter(c -> passwordEncoder.matches(pin == null ? "" : pin, c.getPin()))
                .orElseThrow(() -> new IllegalArgumentException("전화번호 또는 PIN이 올바르지 않습니다."));
        return customer.getId();
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객 정보를 찾을 수 없습니다."));
    }

    /** PIN은 숫자 4자리로 제한한다. */
    private void validatePin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN은 숫자 4자리로 입력하세요.");
        }
    }
}
