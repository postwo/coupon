package com.example.coupon_consumer.component;

import com.example.coupon_core.repository.redis.RedisRepository;
import com.example.coupon_core.repository.redis.dto.CouponIssueRequest;
import com.example.coupon_core.service.CouponIssueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.example.coupon_core.util.couponRedisUtils.getIssueRequestQueueKey;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CouponIssueListener {

    private final RedisRepository redisRepository;
    private final CouponIssueService couponIssueService;

    private final String issueRequestQueueKey = getIssueRequestQueueKey();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 주기적으로 로직 실행
    // 쿠폰 발급 대기열 큐에 적재 되어있는 데이터를 읽어와서 쿠폰 발급
    @Scheduled(fixedDelay = 1000L) //이전 실행이 끝난 시점으로부터 1000ms(1초) 후에 다음 실행을 시작하도록 예약
    public void issue() throws JsonProcessingException {
        log.info("listen........");

        // 쿠폰 발급 대상이 있으면 여기서 처리
        // 큐에 처리할 대상이 남아있는 동안 반복 (1초 동안 최대한 많은 작업 처리 시도)
        while (existCouponIssueTarget()){
            CouponIssueRequest target = getIssueTarget();
            log.info("발급 시작 target: %s".formatted(target));
            // 실제 DB 트랜잭션 처리 (여기서 동시성 이슈를 최종적으로 해결해야 함)
            couponIssueService.issue(target.couponId(), target.userId());
            log.info("발급 완료 target: %s".formatted(target));
            removeIssuedTarget();// 처리가 완료된 항목은 큐에서 제거
        }
    }

    // 쿠폰 발급 큐에 대상이 존재하는지 체크
    private boolean existCouponIssueTarget() {
        // reidsRepository에서 큐에 사이즈를 본다
        // 0 보다 크면 큐에 대상이 남아 있다
        //Redis List의 길이를 확인합니다. 0보다 크면 큐에 데이터가 남아있음을 의미합니다.
        //큐에 작업이 있는지 확인하는 폴링 조건으로 사용.
        // 리스너(CouponIssueListener)가 1초마다 큐를 주기적으로(폴링) 확인하여, **'큐 안에 처리해야 할 쿠폰
        // 발급 요청 데이터가 남아있다'**는 조건(redisRepository.lSize(key) > 0)이 참인 경우에만, 큐에 있는 작업을 꺼내 처리하기 시작한다
        return redisRepository.lSize(issueRequestQueueKey) > 0;
    }

    // 큐에서 앞에 있는 인덱스를 읽어서 가지고 온다
    private CouponIssueRequest getIssueTarget() throws JsonProcessingException {
        // 역직렬화
        //Redis List의 **가장 앞(인덱스 0)**에 있는 데이터를 조회합니다. (제거하지 않음!)
        //큐에서 가장 오래된 요청 데이터를 가져와 ObjectMapper로 역직렬화.
        return objectMapper.readValue(redisRepository.lIndex(issueRequestQueueKey, 0), CouponIssueRequest.class);
    }

    private void removeIssuedTarget() {
        //	Redis List의 가장 앞에 있는 데이터를 제거합니다.
        //couponIssueService.issue()가 성공적으로 완료된 후에만 요청을 큐에서 안전하게 제거하여 중복 처리 방지.
        redisRepository.lPop(issueRequestQueueKey);
    }
}
