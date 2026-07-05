package com.asmanage.domain;

/**
 * 직원 계정 권한.
 * ADMIN(최고관리자)은 계정/마스터데이터 관리를 포함한 전체 기능,
 * STAFF(일반 직원)는 A/S 처리 업무 위주로 제한된다.
 */
public enum Role {
    ADMIN,
    STAFF
}
