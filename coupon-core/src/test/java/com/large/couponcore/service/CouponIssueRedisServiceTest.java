package com.large.couponcore.service;

import com.large.couponcore.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.stream.IntStream;

import static com.large.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponIssueRedisServiceTest extends TestConfig {
    @Autowired
    CouponIssueRedisService couponIssueRedisService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @BeforeEach
    void clear() {
        RKeys rKeys = redissonClient.getKeys();
        Iterable<String> redisKeys = rKeys.getKeysByPattern("*");

        for (String key : redisKeys) {
            redissonClient.getBucket(key).delete();
        }
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하면 true 반환한다")
    void availableTotalIssueQuantity_1() {
        // given
        int totalIssueQuantity = 10;
        long couponId = 1;
        // when
        boolean result = couponIssueRedisService.availableTotalIssueQuantity(totalIssueQuantity, couponId);
        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 모두 소진되면 false 반환한다")
    void availableTotalIssueQuantity_2() {
        // given
        int totalIssueQuantity = 10;
        long couponId = 1;
        IntStream.range(0, totalIssueQuantity).forEach(userId -> {
            redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));
        });
        // when
        boolean result = couponIssueRedisService.availableTotalIssueQuantity(totalIssueQuantity, couponId);
        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하지 않으면 true 반환한다.")
    void availableUserIssueQuantity_1() {
        // given
        long couponId = 1;
        long userId = 1;
        // when
        boolean result = couponIssueRedisService.availableUserIssueQuantity(couponId, userId);
        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하면 false 반환한다.")
    void availableUserIssueQuantity_2() {
        // given
        long couponId = 1;
        long userId = 1;
        redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));
        // when
        boolean result = couponIssueRedisService.availableUserIssueQuantity(couponId, userId);
        // then
        assertFalse(result);
    }
}
