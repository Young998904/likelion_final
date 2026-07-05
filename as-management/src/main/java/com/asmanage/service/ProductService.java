package com.asmanage.service;

import com.asmanage.domain.Product;
import com.asmanage.dto.ProductForm;
import com.asmanage.repository.AsRequestRepository;
import com.asmanage.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 제품 관리 비즈니스 로직.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final AsRequestRepository asRequestRepository;

    /** 전체 제품을 최근 등록순으로 조회. */
    public List<Product> findAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** 수정 화면용 폼 조회. */
    public ProductForm getForm(Long id) {
        Product p = findById(id);
        ProductForm form = new ProductForm();
        form.setId(p.getId());
        form.setName(p.getName());
        form.setModelCode(p.getModelCode());
        form.setNote(p.getNote());
        return form;
    }

    /** 등록 또는 수정. */
    @Transactional
    public void save(ProductForm form) {
        Product p = (form.getId() == null) ? new Product() : findById(form.getId());
        p.setName(form.getName());
        p.setModelCode(form.getModelCode());
        p.setNote(form.getNote());
        productRepository.save(p);
    }

    /** 삭제. 접수 이력이 있으면 삭제를 막는다. */
    @Transactional
    public void delete(Long id) {
        if (asRequestRepository.existsByProductId(id)) {
            throw new IllegalStateException("접수 이력이 있어 삭제할 수 없습니다.");
        }
        productRepository.deleteById(id);
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다."));
    }
}
