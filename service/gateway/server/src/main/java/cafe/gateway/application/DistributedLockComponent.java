package cafe.gateway.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "DistributedLockComponent")
public class DistributedLockComponent {

    private final RedissonClient redissonClient;

    public void exceute(
            String lockName,
            long waitMilliSecond,
            long leaseMilliSecond,
            Runnable logic
    ) {
        RLock lock = redissonClient.getLock(lockName);

        try {
            boolean isLocked = lock.tryLock(waitMilliSecond, leaseMilliSecond, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new IllegalStateException("[" + lockName + "] lock 획득 실패");
            }
            logic.run();    // 분산락으로 보호되는 크리티컬 섹션
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Bean
    public ExecutorService customThreadPool() {
        return Executors.newFixedThreadPool(10);
    }
}
