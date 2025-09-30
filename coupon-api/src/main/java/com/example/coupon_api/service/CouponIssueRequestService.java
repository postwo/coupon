package com.example.coupon_api.service;

import com.example.coupon_api.controller.dto.CouponIssueRequestDto;
import com.example.coupon_core.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;

    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        log.info("쿠폰 발급 완료 couponId: {}, userId: {}", requestDto.couponId(), requestDto.userId());
        couponIssueService.issue(requestDto.couponId(), requestDto.userId());
    }

}
