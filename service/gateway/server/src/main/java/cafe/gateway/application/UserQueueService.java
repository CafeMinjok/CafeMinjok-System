package cafe.gateway.application;

import cafe.gateway.application.dto.RegisterUserResponse;
import cafe.gateway.infrastructure.exception.GatewayErrorCode;
import cafe.gateway.infrastructure.exception.GatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static cafe.gateway.infrastructure.exception.GatewayErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final DistributedLockComponent lockComponent;

    private final String USER_QUEUE_WAIT_KEY = "users:queue:wait";
    private final String USER_QUEUE_PROCEED_KEY = "users:queue:proceed";
    private final String USER_ACTIVE_SET_KEY = "users:active:set";

    // 배포 시에 Docker 환경에서 동적으로 최대 사용자 수 조정
    @Value("${MAX_ACTIVE_USERS: 500}")
    private long MAX_ACTIVE_USERS;
    private final long INACTIVITY_THRESHOLD = 300;

    public Mono<RegisterUserResponse> registerUser(String userId) {
        return reactiveRedisTemplate.opsForZSet()
                .rank(USER_QUEUE_PROCEED_KEY, userId)
                .defaultIfEmpty(-1L)
                .flatMap(rank -> rank >= 0 ? handleProceedUser(userId) : handleNewUSer(userId));
    }

    private Mono<RegisterUserResponse> handleProceedUser(String userId) {
        return updateUserActivityTime(userId)
                .thenReturn(new RegisterUserResponse(0L));
    }

    private Mono<RegisterUserResponse> handleNewUSer(String userId) {
     return reactiveRedisTemplate.opsForZSet().size(USER_ACTIVE_SET_KEY)
             .flatMap(activeUsers -> activeUsers < MAX_ACTIVE_USERS ? addToProceedQueue(userId)
                     : checkAndAddToQueue(userId));
    }

    private Mono<RegisterUserResponse> checkAndAddToQueue(String userId) {
        return reactiveRedisTemplate.opsForZSet().score(USER_QUEUE_WAIT_KEY, userId)
                .defaultIfEmpty(-1.0)
                .flatMap(score -> {
                    if (score >= 0) {
                        return updateWaitQueueScore(userId);    // 이미 대기 큐에 사용자ID가 존재
                    } else {
                        return addToWaitQueue(userId);      // 사용자를 대기 큐에 새로 삽입
                    }
                });
    }

    // 이미 대기 큐에 존재하는 사용자의 rank 증가
    private Mono<RegisterUserResponse> updateWaitQueueScore(String userId) {
        double newScore = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().score(USER_QUEUE_WAIT_KEY, userId)
                .flatMap(oldScore ->
                reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY, userId, newScore)
                        .then(reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY, userId))
                        )
                .map(rank -> new RegisterUserResponse(rank + 1));
    }

    public Mono<RegisterUserResponse> addToProceedQueue(String userId) {
        return Mono.create(sink -> {
            lockComponent.exceute(userId, 1000, 1000, () -> {
                try {
                    addUserToQueue(userId)
                            .doOnSuccess(sink::success)
                            .doOnError(sink::error)
                            .subscribe();
                } catch (Exception e) {
                    sink.error(e);
                }
            });
        });
    }

    private Mono<RegisterUserResponse> addUserToQueue(String userId) {
        var unixTime = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet()
                .add(USER_QUEUE_PROCEED_KEY, userId, unixTime)
                .filter(success -> success)
                .flatMap(success -> {
                    if (success) {
                        return addToActiveSet(userId);
                    } else {
                        return checkAndAddToQueue(userId);
                    }
                });
    }

    private Mono<RegisterUserResponse> addToActiveSet(String userId) {
        return reactiveRedisTemplate.opsForSet()
                .add(USER_ACTIVE_SET_KEY, userId)
                .map(i -> new RegisterUserResponse(0L));
    }

    private Mono<RegisterUserResponse> addToWaitQueue(String userId) {
        var unixTime = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet()
                .add(USER_QUEUE_WAIT_KEY, userId, unixTime)
                .filter(i -> i)
                .switchIfEmpty(Mono.error(new GatewayException(TOO_MANY_REQUESTS)))
                .flatMap(i -> reactiveRedisTemplate.opsForZSet()
                        .rank(USER_QUEUE_WAIT_KEY, userId))
                .map(rank -> new RegisterUserResponse(rank + 1));
    }

    public Mono<Boolean> isAllowed(String userId) {
        return reactiveRedisTemplate.opsForZSet()
                .rank(USER_QUEUE_PROCEED_KEY, userId)
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0)
                .flatMap(isAllowed -> {
                    if (isAllowed) {
                        return updateWaitQueueScore(userId).thenReturn(true);
                    }
                    return Mono.just(false);
                });
    }

    private Mono<Boolean> updateUserActivityTime(String userId) {
        long currentTime = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY, userId, currentTime);
    }
}
