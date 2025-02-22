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

    public void execute(
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
            logic.run();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {

            // 현재 스레드에서 획득한 락만 해제하도록 검증
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 여러 스레드에서 락을 효율적으로 관리할 수 있도록 별도의 쓰레드 풀 제공
    @Bean
    public ExecutorService customThreadPool() { return Executors.newFixedThreadPool(10); }
}
