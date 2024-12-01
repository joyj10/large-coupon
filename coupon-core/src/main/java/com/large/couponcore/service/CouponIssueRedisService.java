package com.large.couponcore.service;

import com.large.couponcore.exception.CouponIssueException;
import com.large.couponcore.repository.redis.RedisRepository;
import com.large.couponcore.repository.redis.entity.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.large.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static com.large.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static com.large.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@Service
@RequiredArgsConstructor
public class CouponIssueRedisService {
    private final RedisRepository redisRepository;

    public void checkCouponIssueQuantity(CouponRedisEntity couponRedisEntity, long userId) {
        if (!availableTotalIssueQuantity(couponRedisEntity.totalQuantity(), couponRedisEntity.id())) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, INVALID_COUPON_ISSUE_QUANTITY.message + " couponId: %s, userId: %s".formatted(couponRedisEntity.id(), userId));
        }

        if (!availableUserIssueQuantity(couponRedisEntity.id(), userId)) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, DUPLICATED_COUPON_ISSUE.message + " couponId: %s, userId: %s".formatted(couponRedisEntity.id(), userId));
        }
    }

    public boolean availableTotalIssueQuantity(Integer totalQuantity, long couponId) {
        if (totalQuantity == null) {
            return true;
        }
        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    public boolean availableUserIssueQuantity(long couponId, long userId) {
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }
}
