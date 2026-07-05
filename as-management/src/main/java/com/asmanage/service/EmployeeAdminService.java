package com.asmanage.service;

import com.asmanage.domain.Employee;
import com.asmanage.domain.Role;
import com.asmanage.dto.EmployeeForm;
import com.asmanage.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 직원 계정 관리(ADMIN 전용) 비즈니스 로직.
 * 계정 삭제는 제공하지 않으며 비활성화로 사용을 중지한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeAdminService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    /** 전체 직원을 등록순으로 조회. */
    public List<Employee> findAll() {
        return employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /** 수정 화면용 폼 조회(비밀번호는 비워서 반환). */
    public EmployeeForm getForm(Long id) {
        Employee e = findById(id);
        EmployeeForm form = new EmployeeForm();
        form.setId(e.getId());
        form.setUsername(e.getUsername());
        form.setName(e.getName());
        form.setRole(e.getRole());
        form.setActive(e.isActive());
        return form;
    }

    /** 신규 계정 등록. */
    @Transactional
    public void create(EmployeeForm form) {
        if (!StringUtils.hasText(form.getPassword())) {
            throw new IllegalArgumentException("초기 비밀번호를 입력하세요.");
        }
        if (employeeRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        Employee e = new Employee();
        e.setUsername(form.getUsername());
        e.setName(form.getName());
        e.setRole(form.getRole());
        e.setActive(form.isActive());
        e.setPassword(passwordEncoder.encode(form.getPassword()));
        employeeRepository.save(e);
    }

    /** 계정 수정. 아이디는 변경하지 않으며, 비밀번호는 입력 시에만 재설정한다. */
    @Transactional
    public void update(EmployeeForm form) {
        Employee e = findById(form.getId());

        // 마지막 활성 최고관리자를 비활성화하거나 권한을 낮추는 것을 막는다
        boolean wasActiveAdmin = e.getRole() == Role.ADMIN && e.isActive();
        boolean willBeActiveAdmin = form.getRole() == Role.ADMIN && form.isActive();
        if (wasActiveAdmin && !willBeActiveAdmin
                && employeeRepository.countByRoleAndActiveTrue(Role.ADMIN) <= 1) {
            throw new IllegalStateException("최소 1명의 활성 최고관리자가 필요합니다.");
        }

        e.setName(form.getName());
        e.setRole(form.getRole());
        e.setActive(form.isActive());
        if (StringUtils.hasText(form.getPassword())) {
            e.setPassword(passwordEncoder.encode(form.getPassword()));
        }
    }

    private Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("직원 계정을 찾을 수 없습니다."));
    }
}
