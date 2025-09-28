package com.example.coupon_core;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@ComponentScan
@EnableJpaAuditing
@EnableAutoConfiguration
public class CouponCoreConfiguration {
}
