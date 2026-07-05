package com.asmanage.controller;

import com.asmanage.domain.AsStatus;
import com.asmanage.repository.AsRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 기본 화면(로그인/대시보드) 라우팅.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AsRequestRepository asRequestRepository;

    /**
     * 루트 접근 시 대시보드로 이동.
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    /**
     * 로그인 화면.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 대시보드. 1단계에서는 상태별 요약 건수만 표시한다.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalCount", asRequestRepository.count());
        model.addAttribute("repairingCount", asRequestRepository.countByStatus(AsStatus.REPAIRING));
        model.addAttribute("completedCount", asRequestRepository.countByStatus(AsStatus.COMPLETED));
        model.addAttribute("unpaidCount", asRequestRepository.countByPaidFalse());
        return "dashboard";
    }
}
