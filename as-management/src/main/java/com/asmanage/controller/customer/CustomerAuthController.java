package com.asmanage.controller.customer;

import com.asmanage.security.CustomerSession;
import com.asmanage.service.CustomerPortalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 고객 포털 인증(회원가입/로그인/로그아웃).
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerPortalService customerPortalService;

    /** 로그인 화면. */
    @GetMapping("/login")
    public String loginForm() {
        return "customer/login";
    }

    /** 로그인 처리(전화+PIN). */
    @PostMapping("/login")
    public String login(@RequestParam String phone, @RequestParam String pin,
                        HttpSession session, RedirectAttributes ra) {
        try {
            Long customerId = customerPortalService.login(phone, pin);
            CustomerSession.set(session, customerId);
            return "redirect:/customer/home";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/login";
        }
    }

    /** 회원가입 화면. */
    @GetMapping("/signup")
    public String signupForm() {
        return "customer/signup";
    }

    /** 회원가입 처리(이름·전화·PIN·배송지). 성공 시 바로 로그인 상태로 진입. */
    @PostMapping("/signup")
    public String signup(@RequestParam String name, @RequestParam String phone, @RequestParam String pin,
                         @RequestParam(required = false) String zipcode,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String addressDetail,
                         HttpSession session, RedirectAttributes ra) {
        try {
            Long customerId = customerPortalService.signup(name, phone, pin, zipcode, address, addressDetail);
            CustomerSession.set(session, customerId);
            return "redirect:/customer/home";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/signup";
        }
    }

    /** 로그아웃. */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        CustomerSession.clear(session);
        return "redirect:/customer/login";
    }
}
