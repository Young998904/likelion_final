package com.asmanage.controller.admin;

import com.asmanage.dto.ProductForm;
import com.asmanage.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 제품 관리 화면(ADMIN 전용). 목록/등록/수정/삭제.
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 제품 목록. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products/list";
    }

    /** 신규 등록 폼. */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ProductForm());
        return "admin/products/form";
    }

    /** 수정 폼. */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", productService.getForm(id));
        return "admin/products/form";
    }

    /** 등록/수정 저장. */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") ProductForm form,
                       BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "admin/products/form";
        }
        boolean isNew = (form.getId() == null);
        productService.save(form);
        ra.addFlashAttribute("message", isNew ? "제품이 등록되었습니다." : "제품 정보가 수정되었습니다.");
        return "redirect:/admin/products";
    }

    /** 삭제. */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.delete(id);
            ra.addFlashAttribute("message", "제품이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
