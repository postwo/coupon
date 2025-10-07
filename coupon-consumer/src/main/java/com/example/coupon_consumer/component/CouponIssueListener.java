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
    @Scheduled(fixedDelay = 1000L)
    public void issue() throws JsonProcessingException {
        log.info("listen........");

        // 쿠폰 발급 대상이 있으면 여기서 처리
        while (existCouponIssueTarget()){
            CouponIssueRequest target = getIssueTarget();
            log.info("발급 시작 target: %s".formatted(target));
            couponIssueService.issue(target.couponId(), target.userId());
            log.info("발급 완료 target: %s".formatted(target));
            removeIssuedTarget();
        }
    }

    // 쿠폰 발급 큐에 대상이 존재하는지 체크
    private boolean existCouponIssueTarget() {
        // reidsRepository에서 큐에 사이즈를 본다
        // 0 보다 크면 큐에 대상이 남아 있다
        return redisRepository.lSize(issueRequestQueueKey) > 0;
    }

    // 큐에서 앞에 있는 인덱스를 읽어서 가지고 온다
    private CouponIssueRequest getIssueTarget() throws JsonProcessingException {
        // 역직렬화
        return objectMapper.readValue(redisRepository.lIndex(issueRequestQueueKey, 0), CouponIssueRequest.class);
    }

    private void removeIssuedTarget() {
        redisRepository.lPop(issueRequestQueueKey);
    }
}
