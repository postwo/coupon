package com.example.coupon_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        Thread.sleep(500);
        return "hello";
    } // rps = 초당 2건 처리 * n(서버에서 동시에 처리할 수 있는 수) ex) 2*200 =400 얼추 비슷하게 나온다
    // thread pool 에서 요청을 처리하는데 톰캣에서 사용하는 max thread pool 은 기본적으로 200 개이다 그러므로 n은 200을 넣는다
    // yml에서 tread max 사이즈를 늘리면 그거에 맞게 변한다 ex) 2 * 400 = 800
}
