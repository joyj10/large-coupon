package com.large.couponcore.repository.redis.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.large.couponcore.exception.CouponIssueException;
import com.large.couponcore.model.code.CouponType;
import com.large.couponcore.model.entity.Coupon;

import java.time.LocalDateTime;

import static com.large.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.large.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

public record CouponRedisEntity(
        Long id,
        CouponType couponType,
        Integer totalQuantity,
        boolean availableIssueQuantity,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueStart,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueEnd
) {
    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.availableIssueQuantity(),
                coupon.getDateIssueStart(),
                coupon.getDateIssueEnd()
        );
    }

    private boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    public void checkIssuableCoupon() {
        if (!availableIssueQuantity) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, INVALID_COUPON_ISSUE_QUANTITY.message + " couponId: %s".formatted(id));
        }
        if (!availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE, INVALID_COUPON_ISSUE_DATE.message + " couponId: %s, issueStart: %s, issueEnd".formatted(id, dateIssueStart, dateIssueEnd));
        }
    }
}
