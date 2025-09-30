package com.example.coupon_api.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(value = NON_NULL) // comment null이 아니면 json에 포함하고 null이면 josn에 포함 안시킨다 = 이거는 자료형마다 다르다 boolean이 있다 boolean은 null 이 나올수 없기 때문에 항상 포함된다
public record CouponIssueResponseDto(boolean isSuccess, String comment) {
}
