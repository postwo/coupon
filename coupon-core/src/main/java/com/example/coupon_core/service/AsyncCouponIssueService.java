package com.example.coupon_core.service;

import com.example.coupon_core.component.DistributeLockExecutor;
import com.example.coupon_core.exception.CouponIssueException;
import com.example.coupon_core.exception.ErrorCode;
import com.example.coupon_core.model.Coupon;
import com.example.coupon_core.repository.redis.RedisRepository;
import com.example.coupon_core.repository.redis.dto.CouponIssueRequest;
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
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(long couponId, long userId) {
        // 1. 유저의 요청을 sorted set 적재
//        String key ="issue.request.sorted_set.couponId=%s".formatted(couponId);
//        redisRepository.zAdd(key,String.valueOf(userId),System.currentTimeMillis()); 이방식은 문제점이 많아서 사용하지 않는다
        // 2. 유저의 요청의 순서를 조회
        // 3. 조회 결과를 선착순 조건과 비교
        // 4. 쿠폰 발급 queue에 적재

        Coupon coupon = couponIssueService.findCoupon(couponId); // 쿠폰 조회
        if (!coupon.availableIssueDate()){
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE,"쿠폰 발급기간이 만료 되었습니다 couponId:%s , issueStart:%s , issueEnd:%s"
                    .formatted(couponId,coupon.getDateIssueStart(),coupon.getDateIssueEnd()));
        }
        // 동시성 제어
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            if (!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
                throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과 합니다. couponId:%s, userId:%s".formatted(couponId, userId));
            }
            if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. user_id: %d, coupon_id: %d".formatted(userId, couponId));
            }
            issueRequest(couponId, userId);
        });
    }

    public void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try{
            String value = objectMapper.writeValueAsString(issueRequest); //objectMapper를 사용한 이유는 redis에 String을 넣어줘야 해서 사용
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            // 쿠폰 발급 큐 적재
            redisRepository.rPush(getIssueRequestQueueKey(),value);
        }catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST,"input : %s".formatted(issueRequest));
        }



    }

}
