package com.large.couponcore.service;


import com.large.couponcore.TestConfig;
import com.large.couponcore.exception.CouponIssueException;
import com.large.couponcore.model.code.CouponType;
import com.large.couponcore.model.entity.Coupon;
import com.large.couponcore.model.entity.CouponIssue;
import com.large.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.large.couponcore.repository.mysql.CouponIssueRepository;
import com.large.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static com.large.couponcore.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CouponIssueServiceTest extends TestConfig {
    @Autowired
    CouponIssueService service;

    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @BeforeEach
    void clean() {
        couponJpaRepository.deleteAllInBatch();
        couponIssueJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하면 예외를 반환한다")
    void saveCouponIssue_1() {
        // given
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();
        couponIssueJpaRepository.save(couponIssue);
        Long couponId = couponIssue.getCouponId();
        Long userId = couponIssue.getUserId();

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, () -> {
            service.saveCouponIssue(couponId, userId);
        });
        assertThat(exception.getErrorCode()).isEqualTo(DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하지 않는다면 쿠폰을 발급한다")
    void saveCouponIssue_2() {
        // given
        long couponId = 1L;
        long userId = 1L;
        // when
        CouponIssue result = service.saveCouponIssue(couponId, userId);
        // then
        assertTrue(couponIssueJpaRepository.findById(result.getId()).isPresent());
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제가 없다면 쿠폰을 발급한다")
    void issue_1() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        // when
        service.issue(coupon.getId(), userId);
        // then
        Coupon couponResult = couponJpaRepository.findById(coupon.getId()).get();
        assertThat(couponResult.getIssuedQuantity()).isEqualTo(1);

        CouponIssue couponIssueResult = couponIssueRepository.findFirstCouponIssue(coupon.getId(), userId);
        assertNotNull(couponIssueResult);
    }


    @Test
    @DisplayName("발급 수량에 문제가 있다면 예외를 반환한다")
    void issue_2() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        Long couponId = coupon.getId();

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, () -> {
            service.issue(couponId, userId);
        });
        assertThat(exception.getErrorCode()).isEqualTo(INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 기한에 문제가 있다면 예외를 반환한다")
    void issue_3() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        Long couponId = coupon.getId();

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, () -> {
            service.issue(couponId, userId);
        });
        assertThat(exception.getErrorCode()).isEqualTo(INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("중복 발급 검증에 문제가 있다면 예외를 반환한다")
    void issue_4() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .build();
        couponIssueJpaRepository.save(couponIssue);
        Long couponId = coupon.getId();

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, () -> {
            service.issue(couponId, userId);
        });
        assertThat(exception.getErrorCode()).isEqualTo(DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않는다면 예외를 반환한다")
    void issue_5() {
        // given
        long userId = 1;
        long couponId = 1;

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, () -> {
            service.issue(couponId, userId);
        });
        assertThat(exception.getErrorCode()).isEqualTo(COUPON_NOT_EXIST);
    }
}
