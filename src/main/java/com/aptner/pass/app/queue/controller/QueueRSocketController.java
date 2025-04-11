package com.aptner.pass.app.queue.controller;

import com.aptner.pass.app.queue.model.QueueResponse;
import com.aptner.pass.app.queue.model.QueueStatusRequest;
import com.aptner.pass.app.queue.model.QueueStatusResponse;
import com.aptner.pass.app.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class QueueRSocketController {

    private final QueueService queueService;

    /**
     * 클라이언트가 requestStream("queue.status", payload) 요청 시 호출됨
     * RSocket이 지속적으로 상태를 스트리밍할 수 있도록 Flux로 응답
     * @return
     */
    @MessageMapping("queue.status")
    public Flux<QueueStatusResponse> streamQueueStatus(@Payload QueueStatusRequest request) {
        log.info("🔥 요청 들어옴 userId: {}", request.userId());
        return queueService.observeQueueStatus(request);
    }

    @MessageMapping("queue.exit")
    public Mono<String> exitQueue(@Payload QueueStatusRequest request) {
        log.info("대기열 나가기 요청 received, userId: {}", request.userId());
        boolean removed = queueService.removeUserFromQueue(request.userId());
        if(removed) {
            return Mono.just("대기열 퇴장 처리 완료");
        } else {
            return Mono.just("대기열에 해당 사용자가 없습니다.");
        }
    }

//    @ConnectMapping
//    public void onConnect(RSocketRequester requester, @Payload QueueStatusRequest request) {
//        requester.metadata().
//        System.out.println("🔥 ConnectMapping 호출됨: requester " + requester);
//        System.out.println("🔥 ConnectMapping 호출됨: QueueStatusRequest " + request);
//    }
}
