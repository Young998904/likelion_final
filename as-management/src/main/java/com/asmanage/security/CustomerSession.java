package com.asmanage.security;

import jakarta.servlet.http.HttpSession;

/**
 * 고객 포털 세션 유틸.
 * 직원용 Spring Security와 분리하여 로그인한 고객 id를 HTTP 세션에 보관한다.
 */
public final class CustomerSession {

    public static final String KEY = "LOGIN_CUSTOMER_ID";

    private CustomerSession() {
    }

    /** 세션에 저장된 고객 id(미로그인 시 null). */
    public static Long getId(HttpSession session) {
        return session == null ? null : (Long) session.getAttribute(KEY);
    }

    public static void set(HttpSession session, Long customerId) {
        session.setAttribute(KEY, customerId);
    }

    public static void clear(HttpSession session) {
        session.removeAttribute(KEY);
    }
}
