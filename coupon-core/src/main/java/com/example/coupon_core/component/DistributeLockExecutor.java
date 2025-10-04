package com.example.coupon_core.component;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DistributeLockExecutor {
    private final RedissonClient redissonClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    //Runnable logic = 실행 시킬 로직
    public void execute(String lockName, long waitMilliSecond, long leaseMilliSecond, Runnable logic) {
        RLock lock = redissonClient.getLock(lockName); // lock 이름 지정
        try {
            // lock 획득 시도
            // lock 획득을 시도하는데 시도 하는데 까지 얼마나 걸리는지 waitMilliSecond로 지정
            // lock을 계속 가지고 있을수는 없으니까 반환 해줘야한다 그시간을 leaseMilliSecond 로 지정
            boolean isLocked = lock.tryLock(waitMilliSecond, leaseMilliSecond, TimeUnit.MILLISECONDS);
            if (!isLocked) { //lock 획득 실패 = lock 을안가지고 일을 처리하면 동시성이슈 터지기 떄문에 에러처리
                throw new IllegalStateException("[" + lockName + "] lock 획득 실패");
            }
            logic.run(); // lock을 획득하면 로직을 실행
        } catch (InterruptedException e) { //tryLock 예외 처리
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // logic 수행이 끝나면 바로 lock을 바로 반환, 로직수행중 예외가 발생해도 lock을 반환
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
