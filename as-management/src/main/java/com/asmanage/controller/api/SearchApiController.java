package com.asmanage.controller.api;

import com.asmanage.dto.CustomerSuggestion;
import com.asmanage.dto.ProductSuggestion;
import com.asmanage.repository.CustomerRepository;
import com.asmanage.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 접수 화면의 고객/제품 자동완성 검색 API(JSON).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchApiController {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    /** 고객 자동완성(이름/연락처 부분일치). */
    @GetMapping("/customers/search")
    public List<CustomerSuggestion> searchCustomers(@RequestParam("q") String q) {
        return customerRepository
                .findTop10ByNameContainingOrPhoneContainingOrderByIdDesc(q, q)
                .stream()
                .map(c -> new CustomerSuggestion(c.getId(), c.getName(), c.getPhone(),
                        c.getZipcode(), c.getAddress(), c.getAddressDetail()))
                .toList();
    }

    /** 제품 자동완성(제품명/모델코드 부분일치). */
    @GetMapping("/products/search")
    public List<ProductSuggestion> searchProducts(@RequestParam("q") String q) {
        return productRepository
                .findTop10ByNameContainingOrModelCodeContainingOrderByIdDesc(q, q)
                .stream()
                .map(p -> new ProductSuggestion(p.getId(), p.getName(), p.getModelCode()))
                .toList();
    }
}
