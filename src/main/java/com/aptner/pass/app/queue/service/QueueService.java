package com.aptner.pass.app.queue.service;

import com.aptner.pass.app.queue.model.QueueStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, Object> passRedisTempalte;

    private static final String QUEUE_KEY_PREFIX = "queue:golf"; // 예시용. 시설/시간별 분리 가능
    private static final int ENTERABLE_LIMIT = 10; // 입장 허용 인원 (예시)

    public Flux<QueueStatusResponse> observeQueueStatus(String token) {
        String queueKey = QUEUE_KEY_PREFIX; // 동적으로 구성 가능

        // 1. Redis ZSET에 사용자 추가 (score = 현재시간)
        passRedisTempalte.opsForZSet().add(queueKey, token, System.currentTimeMillis());

        // 2. TTL 부여 (ZSET 자체 TTL. 사용자 TTL은 별도로 관리 가능)
        passRedisTempalte.expire(queueKey, Duration.ofMinutes(5));

        // 3. Flux.interval 로 상태 주기적으로 push (1초 간격 예시)
        return Flux.interval(Duration.ofSeconds(1))
                .map(tick -> {
                    Long rank = passRedisTempalte.opsForZSet().rank(queueKey, token);
                    if (rank == null) {
                        return QueueStatusResponse.builder().status("KICK").position(-1).build();
                    }

                    int position = rank.intValue() + 1;
                    String status = position <= ENTERABLE_LIMIT ? "READY" : "WAIT";
                    return QueueStatusResponse.builder().status(status).position(position).build();
                })
                .takeUntil(response -> response.status().equals("READY")); // READY되면 Flux 종료
    }
}
