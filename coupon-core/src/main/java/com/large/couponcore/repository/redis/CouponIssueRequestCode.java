package com.large.couponcore.repository.redis;

import com.large.couponcore.exception.CouponIssueException;
import com.large.couponcore.exception.ErrorCode;

public enum CouponIssueRequestCode {
    SUCCESS(1),
    DUPLICATED_COUPON_ISSUE(2),
    INVALID_COUPON_ISSUE_QUANTITY(3),
    ;

    CouponIssueRequestCode(int code) {

    }

    public static CouponIssueRequestCode find(Integer code) {
        return switch (code) {
            case 1 -> SUCCESS;
            case 2 -> DUPLICATED_COUPON_ISSUE;
            case 3 -> INVALID_COUPON_ISSUE_QUANTITY;
            default -> throw new IllegalArgumentException("존재하지 않는 코드입니다. %s".formatted(code));
        };
    }

    public static void checkRequestResult(CouponIssueRequestCode code) {
        switch (code) {
            case INVALID_COUPON_ISSUE_QUANTITY ->
                    throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, ErrorCode.INVALID_COUPON_ISSUE_QUANTITY.message);
            case DUPLICATED_COUPON_ISSUE ->
                    throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, ErrorCode.DUPLICATED_COUPON_ISSUE.message);
            default -> {
                // Do nothing for other cases (if needed).
            }
        }
    }
}
