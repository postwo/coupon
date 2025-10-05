package com.example.coupon_core.util;


public class couponRedisUtils {

    // redis cashe key 생성
    public static String getIssueRequestKey(long couponId){
        return "issue.request.couponId=%s".formatted(couponId);
    }

    // 큐 에 적재할떄 사용할 key
    public static String getIssueRequestQueueKey(){
        return "issue.request";
    }
}
