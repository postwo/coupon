package com.example.coupon_core.repository.redis;

import lombok.RequiredArgsConstructor;
import org.redisson.client.RedisClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String,String> redisTemplate;

    // sorted set 은 문제점이 많기 때문에 이방식은 사용하지 않는다
    public Boolean zAdd(String key, String value, double score){
        // add 를 사용하면 중복값이 요청하면 맨 뒤로 밀리는 현상이 발생 ex) 1 번이 10번 뒤로 이동하는 현상
        // addIfAbsent를 사용하면 이 현상이 해결됨
        // 그 이유는 zadd nx 옵션을 주게 되면 sorted set에 데이터가 없는 경우만 요청을 처리한다
        // 데이터가 있는경우는 요청을 무시
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long sCard(String key){
        return redisTemplate.opsForSet().size(key);
    }

    public Boolean sIsMember(String key, String value){
        return redisTemplate.opsForSet().isMember(key, value);
    }

    // 큐 적재(list 사용)
    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }
}
