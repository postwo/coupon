package com.example.coupon_core.repository.mysql;

import com.example.coupon_core.model.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    // mysql lock 처리
    @Lock(LockModeType.PESSIMISTIC_WRITE) //FOR UPDATE
    @Query("SELECT c FROM Coupon c WHERE c.id = :id ")
    Optional<Coupon> findCouponWithLock(long id);
}
