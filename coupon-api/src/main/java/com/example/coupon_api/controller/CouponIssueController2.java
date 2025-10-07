package com.example.coupon_api.controller;

import com.example.coupon_api.controller.dto.CouponIssueRequestDto;
import com.example.coupon_api.controller.dto.CouponIssueResponseDto;
import com.example.coupon_api.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2")
public class CouponIssueController2 {
    private final CouponIssueRequestService couponIssueRequestService;

    // v1 에서 보다 속도도 빨라지고 동시성 이슈도 해결
    @PostMapping("/issue-async")
    public CouponIssueResponseDto asyncIssueV2(@RequestBody CouponIssueRequestDto dto) {
        couponIssueRequestService.asyncIssueRequestV2(dto);
        return new CouponIssueResponseDto(true, null);
    }
}
