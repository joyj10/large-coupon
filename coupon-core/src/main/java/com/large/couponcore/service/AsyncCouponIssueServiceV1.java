package com.large.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.large.couponcore.component.DistributeLockExecutor;
import com.large.couponcore.exception.CouponIssueException;
import com.large.couponcore.repository.redis.RedisRepository;
import com.large.couponcore.repository.redis.dto.CouponIssueRequest;
import com.large.couponcore.repository.redis.entity.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.large.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.large.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.large.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponCacheService couponCacheService;
    private final ObjectMapper objectMapper;

    public void issue(long couponId, long userId) {
        CouponRedisEntity couponCache = couponCacheService.getCouponCache(couponId);
        couponCache.checkIssuableCoupon();

        distributeLockExecutor.execute(
                "lock_%s".formatted(couponId),
                3000,
                3000,
                () -> {
                    couponIssueRedisService.checkCouponIssueQuantity(couponCache, userId);
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
