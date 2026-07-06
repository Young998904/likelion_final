package com.asmanage.controller.customer;

import com.asmanage.domain.AsRequest;
import com.asmanage.domain.Customer;
import com.asmanage.dto.AsRequestForm;
import com.asmanage.repository.AsRequestRepository;
import com.asmanage.repository.ProductRepository;
import com.asmanage.security.CustomerSession;
import com.asmanage.service.AsRequestService;
import com.asmanage.service.CustomerPortalService;
import com.asmanage.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 고객 포털 A/S 접수(등록/내 목록/상세). 본인 소유 건만 접근 가능.
 */
@Controller
@RequestMapping("/customer/as")
@RequiredArgsConstructor
public class CustomerAsController {

    private final AsRequestService asRequestService;
    private final CustomerPortalService customerPortalService;
    private final ProductRepository productRepository;
    private final AsRequestRepository asRequestRepository;
    private final PaymentService paymentService;

    /** 내 접수 목록. */
    @GetMapping
    public String list(HttpSession session, Model model) {
        Long customerId = CustomerSession.getId(session);
        model.addAttribute("requests", asRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
        return "customer/as-list";
    }

    /** 새 접수 폼(배송지 자동 채움 + 제품 드롭다운). */
    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        Customer customer = customerPortalService.getCustomer(CustomerSession.getId(session));
        model.addAttribute("customer", customer);
        model.addAttribute("products", productRepository.findAll());
        return "customer/as-form";
    }

    /** 접수 등록. 세션 고객 + 선택 제품으로 생성한다. */
    @PostMapping
    public String create(HttpSession session,
                         @RequestParam Long productId,
                         @RequestParam(required = false) String requestContent,
                         @RequestParam(required = false) String deliveryZipcode,
                         @RequestParam(required = false) String deliveryAddress,
                         @RequestParam(required = false) String deliveryAddressDetail,
                         RedirectAttributes ra) {
        try {
            AsRequestForm form = new AsRequestForm();
            form.setCustomerId(CustomerSession.getId(session));
            form.setProductId(productId);
            form.setRequestContent(requestContent);
            form.setDeliveryZipcode(deliveryZipcode);
            form.setDeliveryAddress(deliveryAddress);
            form.setDeliveryAddressDetail(deliveryAddressDetail);
            Long id = asRequestService.create(form);
            ra.addFlashAttribute("message", "접수가 등록되었습니다.");
            return "redirect:/customer/as/" + id;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/as/new";
        }
    }

    /** 내 접수 상세(읽기 전용). 본인 소유가 아니면 목록으로 돌려보낸다. */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        AsRequest request = asRequestService.getDetail(id);
        if (!request.getCustomer().getId().equals(CustomerSession.getId(session))) {
            ra.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/customer/as";
        }
        model.addAttribute("request", request);
        model.addAttribute("notifications", asRequestService.getNotifications(id));
        model.addAttribute("portoneEnabled", paymentService.isPortOneEnabled());
        model.addAttribute("storeId", paymentService.getStoreId());
        model.addAttribute("channelKey", paymentService.getChannelKey());
        return "customer/as-detail";
    }

    /**
     * 결제. 본인 소유·입금대기 건에 대해 결제를 진행한다.
     * PortOne 키 미설정 시 Mock 결제로 즉시 성공 → 입금대기→수리중 자동 전이.
     */
    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        AsRequest request = asRequestService.getDetail(id);
        if (!request.getCustomer().getId().equals(CustomerSession.getId(session))) {
            ra.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/customer/as";
        }
        try {
            paymentService.payByMock(id);
            ra.addFlashAttribute("message", "결제가 완료되었습니다. 수리를 시작합니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/as/" + id;
    }

    /** 현재 상태 조회(JSON) — 고객 화면 폴링용. 본인 소유만. */
    @GetMapping("/{id}/status")
    @ResponseBody
    public Map<String, String> status(@PathVariable Long id, HttpSession session) {
        AsRequest request = asRequestService.getDetail(id);
        if (!request.getCustomer().getId().equals(CustomerSession.getId(session))) {
            return Map.of("status", "FORBIDDEN", "label", "");
        }
        return Map.of("status", request.getStatus().name(), "label", request.getStatus().getLabel());
    }

    /**
     * PortOne 결제 완료 복귀 처리(카카오페이 리다이렉트/팝업 성공 후 호출).
     * paymentId로 서버가 결제를 검증한 뒤 확정한다.
     */
    @GetMapping("/{id}/pay/complete")
    public String payComplete(@PathVariable Long id, @RequestParam String paymentId,
                              HttpSession session, RedirectAttributes ra) {
        AsRequest request = asRequestService.getDetail(id);
        if (!request.getCustomer().getId().equals(CustomerSession.getId(session))) {
            ra.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/customer/as";
        }
        try {
            paymentService.verifyAndConfirm(id, paymentId);
            ra.addFlashAttribute("message", "결제가 완료되었습니다. 수리를 시작합니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "결제 확인 실패: " + e.getMessage());
        }
        return "redirect:/customer/as/" + id;
    }
}

