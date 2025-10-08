package com.example.coupon_core.component;

import com.example.coupon_core.model.event.CouponIssueCompleteEvent;
import com.example.coupon_core.service.CouponCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class CouponEventListener {

    private final CouponCacheService couponCacheService;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());


    //issueComplete 이벤트가 오면 트랜잭션이 커밋된 다음에  리스너가 실행됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void issueComplete(CouponIssueCompleteEvent event) {
        log.info("issue complete. cache refresh start couponId: %s".formatted(event.couponId()));
        couponCacheService.putCouponCache(event.couponId()); // redis에 있는 cache를 업데이트 실행
        // local cache는 서버 각각이 메모리를 가지고 관리
        couponCacheService.putCouponLocalCache(event.couponId()); // local cache에 있는 cache를 업데이트 실행
        log.info("issue complete cache refresh end couponId: %s".formatted(event.couponId()));
    }
}