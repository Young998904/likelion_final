package com.asmanage.controller.admin;

import com.asmanage.dto.RepairPresetForm;
import com.asmanage.service.RepairPresetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 수리내역 프리셋 관리 화면(ADMIN 전용). 목록/등록/수정/삭제.
 */
@Controller
@RequestMapping("/admin/presets")
@RequiredArgsConstructor
public class RepairPresetController {

    private final RepairPresetService repairPresetService;

    /** 프리셋 목록. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("presets", repairPresetService.findAll());
        return "admin/presets/list";
    }

    /** 신규 등록 폼. */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new RepairPresetForm());
        return "admin/presets/form";
    }

    /** 수정 폼. */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", repairPresetService.getForm(id));
        return "admin/presets/form";
    }

    /** 등록/수정 저장. */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") RepairPresetForm form,
                       BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "admin/presets/form";
        }
        boolean isNew = (form.getId() == null);
        repairPresetService.save(form);
        ra.addFlashAttribute("message", isNew ? "프리셋이 등록되었습니다." : "프리셋이 수정되었습니다.");
        return "redirect:/admin/presets";
    }

    /** 삭제. */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            repairPresetService.delete(id);
            ra.addFlashAttribute("message", "프리셋이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/presets";
    }
}
