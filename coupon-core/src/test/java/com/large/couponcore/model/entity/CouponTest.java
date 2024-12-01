package com.large.couponcore.model.entity;

import com.large.couponcore.exception.CouponIssueException;
import com.large.couponcore.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class CouponTest {

    @DisplayName("발급 수량이 남아 있다면 true 반환 한다.")
    @Test
    void availableIssueQuantity_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();

        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertTrue(result);
    }

    @DisplayName("발급 수량이 모두 소진 되었다면 false 반환 한다.")
    @Test
    void availableIssueQuantity_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();

        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertFalse(result);
    }

    @DisplayName("최대 발급 수량이 설정 되지 않았다면 true 반환 한다.")
    @Test
    void availableIssueQuantity_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(100)
                .build();

        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertTrue(result);
    }

    @DisplayName("발급 기간이 시작 되지 않은 경우 false 반환 한다.")
    @Test
    void availableIssueDate_1() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        boolean result = coupon.availableIssueDate();

        // then
        assertFalse(result);
    }

    @DisplayName("발급 기간 중에 발급 요청을 하면 true 반환 한다.")
    @Test
    void availableIssueDate_2() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        boolean result = coupon.availableIssueDate();

        // then
        assertTrue(result);
    }

    @DisplayName("발급 기간이 종료 되면 false 반환 한다.")
    @Test
    void availableIssueDate_3() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        // when
        boolean result = coupon.availableIssueDate();

        // then
        assertFalse(result);
    }

    @DisplayName("발급 수량과 발급 기간이 정상이면 발급에 성공하며, issuedQuantity 1 증가 한다.")
    @Test
    void issue_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        coupon.issue();

        // then
        assertThat(coupon.getIssuedQuantity()).isEqualTo(100);
    }

    @DisplayName("발급 수량을 초과하면 예외를 반환한다.")
    @Test
    void issue_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, coupon::issue);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @DisplayName("발급 기간이 아니면 예외를 반환한다.")
    @Test
    void issue_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, coupon::issue);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @DisplayName("발급 기간이 종료되면 true 반환 한다.")
    @Test
    void isIssueComplete() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().minusDays(2))
                .build();

        // when
        boolean result = coupon.isIssueComplete();

        // then
        assertTrue(result);
    }

    @DisplayName("잔여 발급 가능 수량이 없다면 true 반환 한다.")
    @Test
    void isIssueComplete_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        boolean result = coupon.isIssueComplete();

        // then
        assertTrue(result);
    }

    @DisplayName("발급 기한과 수량이 유효하면 false 반환 한다.")
    @Test
    void isIssueComplete_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        boolean result = coupon.isIssueComplete();

        // then
        assertFalse(result);
    }

}
