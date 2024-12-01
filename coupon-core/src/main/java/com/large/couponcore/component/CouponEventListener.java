package com.large.couponcore.component;

import com.large.couponcore.model.event.CouponIssueCompleteEvent;
import com.large.couponcore.service.CouponCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventListener {
    private final CouponCacheService couponCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void issueComplete(CouponIssueCompleteEvent event) {
        long couponId = event.couponId();
        log.info("issue complete. cache refresh start couponId: %s".formatted(couponId));
        couponCacheService.putCouponCache(couponId);
        couponCacheService.putCouponLocalCache(couponId);
        log.info("issue complete. cache refresh end couponId: %s".formatted(couponId));

    }
}
