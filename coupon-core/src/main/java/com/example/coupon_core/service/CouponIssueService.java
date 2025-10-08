package com.example.coupon_core.service;

import com.example.coupon_core.exception.CouponIssueException;
import com.example.coupon_core.exception.ErrorCode;
import com.example.coupon_core.model.Coupon;
import com.example.coupon_core.model.CouponIssue;
import com.example.coupon_core.model.event.CouponIssueCompleteEvent;
import com.example.coupon_core.repository.mysql.CouponIssueJpaRepository;
import com.example.coupon_core.repository.mysql.CouponIssueRepository;
import com.example.coupon_core.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.coupon_core.exception.ErrorCode.COUPON_NOT_EXIST;
import static com.example.coupon_core.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponIssueRepository couponIssueRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // 여기 락은 redeme 꼭 읽어보기
    // 이거는 AsyncCouponIssueService2에 처리하는 동시에 issue 여기 트랜잭션 처리
    @Transactional
    public void issue(long couponId, long userId) {
            Coupon coupon = findCouponWithLock(couponId); // 쿠폰 조회
            coupon.issue(); // 쿠폰 발급수량 증가
            saveCouponIssue(couponId, userId);
            publishCouponEvent(coupon);
    }

    // 일반 조회 = redis lock 처리할떄는 mysql lock 처리가 필요없기떄문에 일반조회
    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId) {
        return couponJpaRepository.findById(couponId).orElseThrow(()-> new CouponIssueException(COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId)));
    }

    // mysql lock 조회
    @Transactional
    public Coupon findCouponWithLock(long couponId) {
        return couponJpaRepository.findCouponWithLock(couponId).orElseThrow(()-> new CouponIssueException(COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId)));
    }


    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        return couponIssueJpaRepository.save(couponIssue);
    }

    // 유저가 이미 쿠폰을 발급 받았는지 체크
    private void checkAlreadyIssuance(long couponId, long userId) {
        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(couponId, userId);
        if (issue != null) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. user_id: %d, coupon_id: %d".formatted(userId, couponId));
        }
    }

    // 쿠폰이 발급 완료가 되었을때 이벤트가 하나 밣행
    private void publishCouponEvent(Coupon coupon) {
        if (coupon.isIssueComplete()) { // 발급이 모두 소진 된경우 이벤트 발생
            applicationEventPublisher.publishEvent(new CouponIssueCompleteEvent(coupon.getId())); // coupon id를 넣어서 이벤트를 발행
        }
    }

}
