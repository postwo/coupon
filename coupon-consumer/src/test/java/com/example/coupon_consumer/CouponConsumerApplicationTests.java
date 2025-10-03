package com.example.coupon_consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

// ActiveProfiles,TestPropertySource 를 달아준 이유는
// core에 있는 redis 환경 변수 값들을 못가지고 오기 떄문에 사용
// 이렇게 하면 test 에서 발생 하는 에러는 해결
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.name=application-test")
@SpringBootTest
class CouponConsumerApplicationTests {

    @Test
    void contextLoads() {
    }

}