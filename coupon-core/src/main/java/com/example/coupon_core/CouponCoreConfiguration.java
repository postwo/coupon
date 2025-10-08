package com.example.coupon_core;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 스프링 부트 애플리케이션의 핵심 설정을 활성화하고, 애플리케이션에 필요한 컴포넌트들을 스캔하며, JPA 감사(Auditing) 기능을 설정
@ComponentScan
@EnableCaching
@EnableJpaAuditing
@EnableAutoConfiguration
@EnableAspectJAutoProxy(exposeProxy = true)
public class CouponCoreConfiguration {
}
