package com.asmanage.controller.customer;

import com.asmanage.domain.Customer;
import com.asmanage.security.CustomerSession;
import com.asmanage.service.CustomerPortalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 고객 포털 홈(로그인 후 랜딩). 좌: 새 접수 / 우: 내 접수 목록 진입.
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerHomeController {

    private final CustomerPortalService customerPortalService;

    /** 루트 접근 시 홈으로. */
    @GetMapping
    public String index() {
        return "redirect:/customer/home";
    }

    /** 고객 홈. */
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Customer customer = customerPortalService.getCustomer(CustomerSession.getId(session));
        model.addAttribute("customer", customer);
        return "customer/home";
    }
}
