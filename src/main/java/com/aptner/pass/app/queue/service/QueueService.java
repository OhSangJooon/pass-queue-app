package com.aptner.pass.app.queue.service;

import com.aptner.pass.app.queue.model.QueueStatusRequest;
import com.aptner.pass.app.queue.model.QueueStatusResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, Object> passRedisTempalte;

    private static final String QUEUE_KEY_PREFIX = "queue:golf"; // 예시용. 시설/시간별 분리 가능
    private static final int ENTERABLE_LIMIT = 10; // 입장 허용 인원 (예시)

    public Flux<QueueStatusResponse> observeQueueStatus(QueueStatusRequest request) {
        String queueKey = QUEUE_KEY_PREFIX; // 동적으로 구성 가능
        System.out.println("🔁 RSocket 연결됨 - 사용자 토큰: " + request.userId());
        // 1. Redis ZSET에 사용자 추가 (score = 현재시간)
        passRedisTempalte.opsForZSet().add(queueKey, request.userId(), System.currentTimeMillis());

        // 2. TTL 부여 (ZSET 자체 TTL. 사용자 TTL은 별도로 관리 가능)
        passRedisTempalte.expire(queueKey, Duration.ofMinutes(5));

        // 3. Flux.interval 로 상태 주기적으로 push (5초 간격 예시)
        return Flux.interval(Duration.ofSeconds(5))
                .map(tick -> {
                    Long rank = passRedisTempalte.opsForZSet().rank(queueKey, request.userId());
                    if (rank == null) {
                        return QueueStatusResponse.builder().status("KICK").position(-1).build();
                    }

                    int position = rank.intValue() + 1;
                    String status = position <= ENTERABLE_LIMIT ? "READY" : "WAIT";
                    return QueueStatusResponse.builder().status(status).position(position).build();
                })
                .takeUntil(response -> response.status().equals("READY")); // READY되면 Flux 종료
    }

    /**
     * Redis ZSET에서 사용자 삭제.
     * @param userId 삭제할 사용자 ID
     * @return 삭제된 항목 수가 0보다 크면 true, 아니면 false
     */
    public boolean removeUserFromQueue(String userId) {
        Long removed = passRedisTempalte.opsForZSet().remove(QUEUE_KEY_PREFIX, userId);
        return (removed != null && removed > 0);
    }
}
