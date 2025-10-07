package com.example.coupon_consumer;

import com.example.coupon_core.CouponCoreConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test") // yml에 on-profile 이름이 test인걸 사용한다는 의미이다
@TestPropertySource(properties = "spring.config.name=application-test")
@SpringBootTest(classes = CouponCoreConfiguration.class) // application.yml 파일을 사용한다는 의미이다
public class TestConfig {
}
