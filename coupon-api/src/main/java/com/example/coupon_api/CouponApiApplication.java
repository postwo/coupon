package com.example.coupon_api;

import com.example.coupon_core.CouponCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

//다른 패키지나 모듈에 정의된 **설정 클래스(Configuration Class)**를 현재 애플리케이션에 가져와서 사용
//coupon-api 모듈은 coupon-core 모듈에 있는 CouponCoreConfiguration에 정의된 모든 빈(Bean)들을 함께 사용하겠다는 의미
@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponApiApplication {

	public static void main(String[] args) {
		// yml 파일이나 properties 설정 파일을 읽어와서 이 서버에 적용 시킨다는 의미
		System.setProperty("spring.config.name", "application-core,application-api");
		SpringApplication.run(CouponApiApplication.class, args);
	}

}
