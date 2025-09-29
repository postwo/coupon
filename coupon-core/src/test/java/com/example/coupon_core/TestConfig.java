package com.example.coupon_core;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test") // yml에 on-profile 이름이 test인걸 사용한다는 의미이다
@SpringBootTest(classes = CouponCoreConfiguration.class) // application.yml 파일을 사용한다는 의미이다
public class TestConfig {
}
