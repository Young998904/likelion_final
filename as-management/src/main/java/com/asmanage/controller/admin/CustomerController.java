package com.asmanage.controller.admin;

import com.asmanage.dto.CustomerForm;
import com.asmanage.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 고객 관리 화면(ADMIN 전용). 목록/등록/수정/삭제.
 */
@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /** 고객 목록. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "admin/customers/list";
    }

    /** 신규 등록 폼. */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new CustomerForm());
        return "admin/customers/form";
    }

    /** 수정 폼. */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", customerService.getForm(id));
        return "admin/customers/form";
    }

    /** 등록/수정 저장(id 유무로 구분). */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") CustomerForm form,
                       BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            // 검증 실패 시 폼을 다시 보여준다
            return "admin/customers/form";
        }
        boolean isNew = (form.getId() == null);
        customerService.save(form);
        ra.addFlashAttribute("message", isNew ? "고객이 등록되었습니다." : "고객 정보가 수정되었습니다.");
        return "redirect:/admin/customers";
    }

    /** 삭제(접수 이력이 있으면 서비스에서 막고 오류 메시지 표시). */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            customerService.delete(id);
            ra.addFlashAttribute("message", "고객이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/customers";
    }
}
