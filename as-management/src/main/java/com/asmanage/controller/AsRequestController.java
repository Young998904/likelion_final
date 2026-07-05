package com.asmanage.controller;

import com.asmanage.domain.AsStatus;
import com.asmanage.domain.Employee;
import com.asmanage.domain.Role;
import com.asmanage.dto.AsRequestForm;
import com.asmanage.repository.EmployeeRepository;
import com.asmanage.repository.ProductRepository;
import com.asmanage.repository.RepairPresetRepository;
import com.asmanage.service.AsRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * A/S 접수 등록·목록·상세 및 상태 전이 처리(로그인한 직원 전체 이용).
 */
@Controller
@RequestMapping("/as")
@RequiredArgsConstructor
public class AsRequestController {

    private final AsRequestService asRequestService;
    private final RepairPresetRepository repairPresetRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;

    /** 접수 목록(상태/고객명/담당자 필터). STAFF는 본인 담당 건만 조회된다. */
    @GetMapping
    public String list(@RequestParam(required = false) AsStatus status,
                       @RequestParam(required = false) String customerKeyword,
                       @RequestParam(required = false) Long assigneeId,
                       Principal principal, Model model) {
        Employee me = employeeRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 확인할 수 없습니다."));
        // STAFF는 담당자 필터를 본인으로 고정한다(관리자는 전체/선택 담당자 조회)
        boolean staffOnly = me.getRole() == Role.STAFF;
        Long effectiveAssigneeId = staffOnly ? me.getId() : assigneeId;

        model.addAttribute("requests", asRequestService.list(status, customerKeyword, effectiveAssigneeId));
        model.addAttribute("statuses", AsStatus.values());
        model.addAttribute("employees", employeeRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("status", status);
        model.addAttribute("customerKeyword", customerKeyword);
        model.addAttribute("assigneeId", assigneeId);
        model.addAttribute("staffOnly", staffOnly);
        return "as/list";
    }

    /** 신규 접수 폼. */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new AsRequestForm());
        // 제품은 목록에서 바로 선택할 수 있도록 전체 목록 전달
        model.addAttribute("allProducts", productRepository.findAll());
        return "as/form";
    }

    /** 신규 접수 등록. */
    @PostMapping
    public String create(@ModelAttribute("form") AsRequestForm form, RedirectAttributes ra) {
        try {
            Long id = asRequestService.create(form);
            ra.addFlashAttribute("message", "접수가 등록되었습니다.");
            return "redirect:/as/" + id;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/as/new";
        }
    }

    /** 접수 상세/처리 화면. */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("request", asRequestService.getDetail(id));
        model.addAttribute("notifications", asRequestService.getNotifications(id));
        model.addAttribute("presets", repairPresetRepository.findAll());
        model.addAttribute("employees", employeeRepository.findByActiveTrueOrderByNameAsc());
        return "as/detail";
    }

    /** 담당자 지정. */
    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id, @RequestParam Long employeeId, RedirectAttributes ra) {
        run(() -> asRequestService.assign(id, employeeId), ra, "담당자가 지정되었습니다.");
        return "redirect:/as/" + id;
    }

    /** 수리내역 추가(프리셋). */
    @PostMapping("/{id}/items/preset")
    public String addPreset(@PathVariable Long id, @RequestParam Long presetId, RedirectAttributes ra) {
        run(() -> asRequestService.addPresetItem(id, presetId), ra, "수리내역이 추가되었습니다.");
        return "redirect:/as/" + id;
    }

    /** 수리내역 추가(수동). */
    @PostMapping("/{id}/items/manual")
    public String addManual(@PathVariable Long id, @RequestParam String name,
                            @RequestParam(defaultValue = "0") int price, RedirectAttributes ra) {
        run(() -> asRequestService.addManualItem(id, name, price), ra, "수리내역이 추가되었습니다.");
        return "redirect:/as/" + id;
    }

    /** 수리내역 삭제. */
    @PostMapping("/{id}/items/{itemId}/delete")
    public String removeItem(@PathVariable Long id, @PathVariable Long itemId, RedirectAttributes ra) {
        run(() -> asRequestService.removeItem(id, itemId), ra, "수리내역이 삭제되었습니다.");
        return "redirect:/as/" + id;
    }

    /** 접수 → 수리중. */
    @PostMapping("/{id}/start")
    public String start(@PathVariable Long id, RedirectAttributes ra) {
        run(() -> asRequestService.startRepair(id), ra, "수리를 시작했습니다.");
        return "redirect:/as/" + id;
    }

    /** 수리중 → 입금대기. */
    @PostMapping("/{id}/request-payment")
    public String requestPayment(@PathVariable Long id, RedirectAttributes ra) {
        run(() -> asRequestService.requestPayment(id), ra, "입금을 요청했습니다.");
        return "redirect:/as/" + id;
    }

    /** 입금대기 → 배송대기. */
    @PostMapping("/{id}/confirm-payment")
    public String confirmPayment(@PathVariable Long id, RedirectAttributes ra) {
        run(() -> asRequestService.confirmPayment(id), ra, "입금을 확인했습니다.");
        return "redirect:/as/" + id;
    }

    /** 송장 입력. */
    @PostMapping("/{id}/tracking")
    public String tracking(@PathVariable Long id, @RequestParam(required = false) String courier,
                           @RequestParam String trackingNo, RedirectAttributes ra) {
        run(() -> asRequestService.saveTracking(id, courier, trackingNo), ra, "송장이 등록되었습니다.");
        return "redirect:/as/" + id;
    }

    /** 배송대기 → 완료. */
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes ra) {
        run(() -> asRequestService.complete(id), ra, "A/S를 완료 처리했습니다.");
        return "redirect:/as/" + id;
    }

    /** 처리 로직 실행 공통 래퍼: 성공/실패 메시지를 플래시로 전달. */
    private void run(Runnable action, RedirectAttributes ra, String successMessage) {
        try {
            action.run();
            ra.addFlashAttribute("message", successMessage);
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
    }
}
