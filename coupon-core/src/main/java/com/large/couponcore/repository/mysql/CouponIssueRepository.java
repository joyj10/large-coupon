package com.large.couponcore.repository.mysql;

import com.large.couponcore.model.entity.CouponIssue;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.large.couponcore.model.entity.QCouponIssue.couponIssue;

@Repository
@RequiredArgsConstructor
public class CouponIssueRepository {
    private final JPAQueryFactory queryFactory;

    public CouponIssue findFirstCouponIssue(long couponId, long userId) {
        return queryFactory.selectFrom(couponIssue)
                .where(
                        couponIssue.couponId.eq(couponId),
                        couponIssue.userId.eq(userId)
                )
                .fetchOne();
    }
}
