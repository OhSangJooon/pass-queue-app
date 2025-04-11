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
    public Flux<QueueStatusResponse> streamQueueStatus(@Payload QueueStatusRequest request, Flux<QueueStatusRequest> flux) {
        log.info("🔥 요청 들어옴 userId: {}", request.userId());
//        return queueService.observeQueueStatus(token);
        queueService.addFlux(flux);
        return queueService.getQueueStatusStream(request.userId());
//        return Flux.just(new QueueStatusResponse("WAIT", 99));
    }

    @ConnectMapping
    public void onConnect(RSocketRequester requester, @Payload QueueStatusRequest request) {
        System.out.println("🔥 ConnectMapping 호출됨: requester " + requester);
        System.out.println("🔥 ConnectMapping 호출됨: QueueStatusRequest " + request);
    }
}
