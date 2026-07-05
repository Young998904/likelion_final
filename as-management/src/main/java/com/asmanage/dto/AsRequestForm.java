package com.asmanage.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * A/S 신규 접수 등록 폼.
 * 고객/제품은 자동완성으로 기존 항목을 선택(customerId/productId)하거나,
 * 없으면 신규 값(newCustomer..., newProduct...)을 입력해 인라인 등록한다.
 */
@Getter
@Setter
public class AsRequestForm {

    // 기존 고객 선택 시 id (신규 등록이면 null)
    private Long customerId;
    private String newCustomerName;
    private String newCustomerPhone;
    private String newCustomerZipcode;
    private String newCustomerAddress;
    private String newCustomerAddressDetail;

    // 기존 제품 선택 시 id (신규 등록이면 null)
    private Long productId;
    private String newProductName;
    private String newProductModelCode;

    // 고객 요청 내용
    private String requestContent;

    // 배송지(기본값은 고객 주소, 수정 가능)
    private String deliveryZipcode;
    private String deliveryAddress;
    private String deliveryAddressDetail;
}
