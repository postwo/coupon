package com.example.coupon_core.service;

import com.example.coupon_core.component.DistributeLockExecutor;
import com.example.coupon_core.exception.CouponIssueException;
import com.example.coupon_core.repository.redis.RedisRepository;
import com.example.coupon_core.repository.redis.dto.CouponIssueRequest;
import com.example.coupon_core.repository.redis.dto.CouponRedisEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.coupon_core.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.example.coupon_core.util.couponRedisUtils.getIssueRequestKey;
import static com.example.coupon_core.util.couponRedisUtils.getIssueRequestQueueKey;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueService2 {

    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void issue(long couponId, long userId) {
        // 쿠폰 캐시를 통한 유효성 검증
//        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId); // 캐시를 가지고 온다
        //getCouponLocalCache localcache를 가지고 와서 coupon에 저장
        CouponRedisEntity coupon = couponCacheService.getCouponLocalCache(couponId); // 로컬 캐시 생성후 그동안은 redis를 안본다 = 좀 더 빠르게 처리 가능 , redis로 들어가는 트래픽도 줄어든다
        // redis로 들어가는 트래픽이 석착순 조건을 만족하지 못하는 사람들에 대해서 제어를 할 수 없을까 라는 고민에서
        // local cache를 활용하는 구조를 사용
        coupon.checkIssuableCoupon(); // 캐시로 부터 유효성 검사
        issueRequest(couponId, userId,coupon.totalQuantity()); // 쿠폰 발급 큐(대기열)에 적재
    }



    public void issueRequest(long couponId, long userId, Integer totalIssueQuantity) {
        if (totalIssueQuantity == null){
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE); //Integer.MAX_VALUE를 넘기는 이유는 검증을 우회하기 위해
        }
        redisRepository.issueRequest(couponId, userId, totalIssueQuantity);
    }

}
