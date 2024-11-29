package com.large.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.large.couponcore.component.DistributeLockExecutor;
import com.large.couponcore.exception.CouponIssueException;
import com.large.couponcore.model.entity.Coupon;
import com.large.couponcore.repository.redis.RedisRepository;
import com.large.couponcore.repository.redis.dto.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.large.couponcore.exception.ErrorCode.*;
import static com.large.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.large.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final ObjectMapper objectMapper;

    public void issue(long couponId, long userId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        if (!coupon.availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE, INVALID_COUPON_ISSUE_DATE.message + " couponId: %s, issueStart: %s, issueEnd".formatted(couponId, coupon.getDateIssueStart(), coupon.getDateIssueEnd()));
        }

        distributeLockExecutor.execute(
                "lock_%s".formatted(couponId),
                3000,
                3000,
                () -> {
                    if (!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
                        throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, INVALID_COUPON_ISSUE_QUANTITY.message + " couponId: %s, userId: %s".formatted(couponId, userId));
                    }

                    if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                        throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, DUPLICATED_COUPON_ISSUE.message + " couponId: %s, userId: %s".formatted(couponId, userId));
                    }

                    issueRequest(couponId, userId);
                }
            );
    }

    private void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));

            String value = objectMapper.writeValueAsString(issueRequest);
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }

}
