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


         /*
        checkCouponIssueQuantity 동작 순서
        1. totalQuantity > redisRepository.sCard(key); 쿠폰 발급 수량 검증
        2. !redisRepository.sIsMember(key,String.valueOf(userId)) 중복 발급 검증
        3. issueRequest에서 redisRepository.sAdd 쿠폰 발급 요청을 set에 저장
        4. redisRepository.rPush 쿠폰 발급 큐 적재
        위 4가지를 lock을 걸지 않고 한 번에 처리하는 방식으로 병목현상을 해결 할 수 있지 않을까 싶다
        그래서 redis 스크립트에 위 4가지 과정을 담아서 처리

        중요) redis 스크립트에 위 4가지 과정을 담아서 실행 그때 redis key,arg를 담아서 실행
        이 실행 자체는 하나의 원자성을 띄고 있어서 실행이 되는중간에 다른 커맨드들이 실행이 될수없다
        redis는 싱글스레드로 실행됨 고로 동시성 이슈를 해결할수 있다
         */
        // 이거는 lock 없는 버전 = lock을 없애고 redis에 스크립트를 활용해서 변목현상 해결

        // 동시성 제어를 하면 어쩔수없이 성능면에서 떨어지는 현상이 발생 = 병목현상
        // 이걸 사용 해서 부하 테스트를 하면 lock 획득 실패 가 발생 그러므로 redis에서 lock을 관리
        // distributeLockExecutor.execute 여기서 병목현상이 발생하는거 같다 그 이유는 lock 을 걸고 해제하는 부분에서 성능 저하를 발생
        // 확인 방법으로는 lock을 지우고 코드를 실행해 보면 된다
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
