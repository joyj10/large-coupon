package com.large.couponcore.model.entity;

import com.large.couponcore.exception.CouponIssueException;
import com.large.couponcore.model.BaseTimeEntity;
import com.large.couponcore.model.code.CouponType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.large.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.large.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dataIssuedStart;

    @Column(nullable = false)
    private LocalDateTime dataIssuedEnd;

    public boolean availableIssueQuantity() {
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dataIssuedStart.isBefore(now) && dataIssuedEnd.isAfter(now);
    }

    public void issue() {
        if (!availableIssueQuantity()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY,
                    "발급 가능 수량을 초과했습니다. total : %s, issued: %s".formatted(totalQuantity, issuedQuantity));
        }

        if (!availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 일자가 아닙니다. request %s, issueStart: %s, issueEnd: %s".formatted(LocalDateTime.now(), dataIssuedStart, dataIssuedEnd));
        }

        issuedQuantity++;
    }
}