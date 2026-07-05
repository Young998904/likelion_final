package com.asmanage.service;

import com.asmanage.domain.Customer;
import com.asmanage.dto.CustomerForm;
import com.asmanage.repository.AsRequestRepository;
import com.asmanage.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 고객 관리 비즈니스 로직.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AsRequestRepository asRequestRepository;

    /** 전체 고객을 최근 등록순으로 조회. */
    public List<Customer> findAll() {
        return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** 수정 화면용 폼 조회. */
    public CustomerForm getForm(Long id) {
        Customer c = findById(id);
        CustomerForm form = new CustomerForm();
        form.setId(c.getId());
        form.setName(c.getName());
        form.setPhone(c.getPhone());
        form.setZipcode(c.getZipcode());
        form.setAddress(c.getAddress());
        form.setAddressDetail(c.getAddressDetail());
        return form;
    }

    /** 등록(id 없음) 또는 수정(id 있음). */
    @Transactional
    public void save(CustomerForm form) {
        Customer c = (form.getId() == null) ? new Customer() : findById(form.getId());
        c.setName(form.getName());
        c.setPhone(form.getPhone());
        c.setZipcode(form.getZipcode());
        c.setAddress(form.getAddress());
        c.setAddressDetail(form.getAddressDetail());
        customerRepository.save(c);
    }

    /** 삭제. 접수 이력이 있으면 삭제를 막는다. */
    @Transactional
    public void delete(Long id) {
        if (asRequestRepository.existsByCustomerId(id)) {
            throw new IllegalStateException("접수 이력이 있어 삭제할 수 없습니다.");
        }
        customerRepository.deleteById(id);
    }

    private Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
    }
}
