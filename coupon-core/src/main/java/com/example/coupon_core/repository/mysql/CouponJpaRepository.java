package com.example.coupon_core.repository.mysql;

import com.example.coupon_core.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
