package com.asmanage.controller.admin;

import com.asmanage.domain.Role;
import com.asmanage.dto.EmployeeForm;
import com.asmanage.service.EmployeeAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 직원 계정 관리 화면(ADMIN 전용). 목록/등록/수정(삭제 없음).
 */
@Controller
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeAdminService employeeAdminService;

    /** 직원 목록. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employeeAdminService.findAll());
        return "admin/employees/list";
    }

    /** 신규 등록 폼. */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new EmployeeForm());
        model.addAttribute("roles", Role.values());
        return "admin/employees/form";
    }

    /** 수정 폼. */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", employeeAdminService.getForm(id));
        model.addAttribute("roles", Role.values());
        return "admin/employees/form";
    }

    /** 등록/수정 저장. */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") EmployeeForm form,
                       BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/employees/form";
        }
        try {
            boolean isNew = (form.getId() == null);
            if (isNew) {
                employeeAdminService.create(form);
                ra.addFlashAttribute("message", "직원 계정이 등록되었습니다.");
            } else {
                employeeAdminService.update(form);
                ra.addFlashAttribute("message", "직원 계정이 수정되었습니다.");
            }
            return "redirect:/admin/employees";
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 아이디 중복·마지막 관리자 보호 등은 폼으로 되돌려 오류 표시
            model.addAttribute("roles", Role.values());
            model.addAttribute("formError", e.getMessage());
            return "admin/employees/form";
        }
    }
}
