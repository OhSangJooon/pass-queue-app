package com.aptner.pass.app.queue.controller;

import com.aptner.pass.app.queue.model.QueueStatusResponse;
import com.aptner.pass.app.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class QueueRSocketController {

    private final QueueService queueService;

    /**
     * 클라이언트가 requestStream("queue.status", payload) 요청 시 호출됨
     * RSocket이 지속적으로 상태를 스트리밍할 수 있도록 Flux로 응답
     * @return
     */
    @MessageMapping("queue.status")
    public Flux<QueueStatusResponse> streamQueueStatus(String token) {
        return queueService.observeQueueStatus(token); // 이후 Step 4에서 구현
    }
}
