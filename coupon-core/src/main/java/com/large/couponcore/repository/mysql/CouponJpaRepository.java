package com.large.couponcore.repository.mysql;

import com.large.couponcore.model.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
