package com.asmanage.controller;

import com.asmanage.service.DashboardService;
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

    private final DashboardService dashboardService;

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
     * 대시보드. 상태별 요약 통계와 차트용 데이터를 전달한다.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("data", dashboardService.load());
        return "dashboard";
    }
}
