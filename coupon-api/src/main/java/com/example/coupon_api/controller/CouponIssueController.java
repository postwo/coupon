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
@RequestMapping("/v1")
public class CouponIssueController {
    private final CouponIssueRequestService couponIssueRequestService;

    // mysql,redis lock 처리
    @PostMapping("/issue")
    public CouponIssueResponseDto issueV1(@RequestBody CouponIssueRequestDto dto) {
        couponIssueRequestService.issueRequestV1(dto);
        return new CouponIssueResponseDto(true, null);
    }

    // redis set
    @PostMapping("/issue-async")
    public CouponIssueResponseDto asyncIssueV1(@RequestBody CouponIssueRequestDto dto) {
        couponIssueRequestService.asyncIssueRequestV1(dto);
        return new CouponIssueResponseDto(true, null);
    }
}
