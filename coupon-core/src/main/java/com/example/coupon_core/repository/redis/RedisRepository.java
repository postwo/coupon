package com.example.coupon_core.repository.redis;

import com.example.coupon_core.exception.CouponIssueException;
import com.example.coupon_core.repository.redis.dto.CouponIssueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.coupon_core.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.example.coupon_core.util.couponRedisUtils.getIssueRequestKey;
import static com.example.coupon_core.util.couponRedisUtils.getIssueRequestQueueKey;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String,String> redisTemplate;
    private final RedisScript<String> issueScript = issueRequestScript();
    private final String issueRequestQueueKey = getIssueRequestQueueKey();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    public String lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }


    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }


    // 스크립트를 실행시키는 메서드
    public void issueRequest(long couponId, long userId, int totalIssueQuantity){
        String issueRequestKey = getIssueRequestKey(couponId);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);
        try{
          //code 는   return '2',return '1', return '3' 이다
          String code = redisTemplate.execute(
                  issueScript,
                  // list는 KEYS[1],KEYS[2] 이렇게 들어간다
                  List.of(issueRequestKey,issueRequestQueueKey),
                  String.valueOf(userId), //ARGV[1]
                  String.valueOf(totalIssueQuantity), // ARGV[2]
                  // 직렬화
                  objectMapper.writeValueAsString(couponIssueRequest) //ARGV[3]
          );

          // validation
          CouponIssueRequestCode.checkRequestResult(CouponIssueRequestCode.find(code));

        }catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST,"input : %s".formatted(couponIssueRequest));
        }
    }

    // redis script
    // ARGV = userId 같은게 들어간다
    private RedisScript<String> issueRequestScript(){
        String script = """
                        if redis.call('SISMEMBER',KEYS[1],ARGV[1]) == 1 then 
                            return '2'
                        end
                        
                        if tonumber(ARGV[2]) > redis.call('SCARD',KEYS[1]) then
                            redis.call('SADD',KEYS[1],ARGV[1])
                            redis.call('RPUSH',KEYS[2],ARGV[3])
                            return '1'
                        
                        end
                        
                        return '3'
                        """;
        return RedisScript.of(script, String.class);
                          
                        
                
    }
}
