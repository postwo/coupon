package com.example.coupon_api.service;

import com.example.coupon_api.controller.dto.CouponIssueRequestDto;
import com.example.coupon_core.component.DistributeLockExecutor;
import com.example.coupon_core.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor; // redis lock 처리

    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        //"lock_" + requestDto.couponId() = lock 이름
        // execute = 여기에서 redis lock
//        distributeLockExecutor.execute ("lock_" + requestDto.couponId(),10000,10000, ()-> { // redis lock 처리
//            couponIssueService.issue(requestDto.couponId(), requestDto.userId());
//        });

        // mysql lock 처리
        couponIssueService.issue(requestDto.couponId(), requestDto.userId());
        log.info("쿠폰 발급 완료 couponId: {}, userId: {}", requestDto.couponId(), requestDto.userId());
    }

}
