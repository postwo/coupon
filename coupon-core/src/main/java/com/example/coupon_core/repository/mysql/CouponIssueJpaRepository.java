package com.example.coupon_core.repository.mysql;

import com.example.coupon_core.model.Coupon;
import com.example.coupon_core.model.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueJpaRepository extends JpaRepository<CouponIssue, Long> {
}
