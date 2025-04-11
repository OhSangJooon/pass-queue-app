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
    // Sinks.Many를 사용하여 여러 구독자에게 동시에 push할 수 있음.
    private final Sinks.Many<QueueStatusResponse> statusSink = Sinks.many().multicast().directBestEffort();

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


    public void addFlux(Flux<QueueStatusRequest> flux) {
        

    }
    // 클라이언트가 구독하면 이 Flux를 통해 상태 업데이트를 받게 됨.
    public Flux<QueueStatusResponse> getQueueStatusStream(String userId) {
        // 실전에서는 userId별 필터링을 추가하거나,
        // 전체 대기열 상태를 반환한 후 클라이언트에서 필요한 부분만 사용하게 할 수 있습니다.
        return statusSink.asFlux();
    }

    // 외부(예, Redis ZSET 변경 이벤트)에 의해 상태가 업데이트되면 이 메서드를 호출하여 새로운 이벤트를 emit합니다.
    public void updateQueueStatus(QueueStatusResponse newStatus) {
        statusSink.tryEmitNext(newStatus);
    }

    // 예제: 서버가 주기적으로 상태 업데이트를 발생시키도록 설정 (실제 환경에서는 Redis ZSET 이벤트 등으로 대체)
    @PostConstruct
    public void simulateQueueUpdates() {
        Flux.interval(Duration.ofSeconds(1))
                .map(i -> {
                    log.info("update queue  {}" , i);
                    return new QueueStatusResponse("WAIT", (int) (Math.random() * 100));
                })
                .subscribe(this::updateQueueStatus);
    }
}
