package com.large.couponconsumer.componont;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.large.couponcore.repository.redis.RedisRepository;
import com.large.couponcore.repository.redis.dto.CouponIssueRequest;
import com.large.couponcore.service.CouponIssueService;
import com.large.couponcore.util.CouponRedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.large.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CouponIssueListener {

    private final RedisRepository redisRepository;
    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper;
    private final String issueRequestQueueKey = getIssueRequestQueueKey();

    @Scheduled(fixedDelay = 1000L)
    public void issue() throws JsonProcessingException {
        log.info("listen...");
        while (existCouponIssueTarget()) {
            CouponIssueRequest target = getIssueTarget();
            log.info("발급 시작 target: %s".formatted(target));
            couponIssueService.issue(target.couponId(), target.userId());
            log.info("발급 완료 target: %s".formatted(target));
            removeIssuedTarget();
        }
    }

    private boolean existCouponIssueTarget() {
        return redisRepository.lSize(issueRequestQueueKey) > 0;
    }

    private CouponIssueRequest getIssueTarget() throws JsonProcessingException {
        String value = redisRepository.lIndex(issueRequestQueueKey, 0);
        return objectMapper.readValue(value, CouponIssueRequest.class);
    }

    private void removeIssuedTarget() {
        redisRepository.lPop(issueRequestQueueKey);
    }


}
