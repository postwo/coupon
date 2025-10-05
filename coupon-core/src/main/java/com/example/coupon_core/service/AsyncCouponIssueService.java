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

import static com.example.coupon_core.exception.ErrorCode.*;
import static com.example.coupon_core.util.couponRedisUtils.getIssueRequestKey;
import static com.example.coupon_core.util.couponRedisUtils.getIssueRequestQueueKey;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueService {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponCacheService couponCacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void issue(long couponId, long userId) {
        // 쿠폰 캐시를 통한 유효성 검증
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId); // 캐시를 가지고 온다
        coupon.checkIssuableCoupon(); // 캐시로 부터 유효성 검사
        // 동시성 제어
        // 이걸 사용 해서 부하 테스트를 하면 lock 획득 실패 가 발생 그러므로 redis에서 lock을 관리 
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId); // 수량 검증
            issueRequest(couponId, userId);
        });
    }

    public void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try{
            String value = objectMapper.writeValueAsString(issueRequest); //objectMapper를 사용한 이유는 redis에 String을 넣어줘야 해서 사용
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId)); // 유저 요청을 저장하는 set
            // 쿠폰 발급 큐 적재
            redisRepository.rPush(getIssueRequestQueueKey(),value);
        }catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST,"input : %s".formatted(issueRequest));
        }



    }

}
