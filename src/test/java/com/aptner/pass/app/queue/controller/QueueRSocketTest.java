package com.aptner.pass.app.queue.controller;

import com.aptner.pass.app.queue.model.QueueStatusRequest;
import com.aptner.pass.app.queue.model.QueueStatusResponse;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.logging.Logger;

@SpringBootTest
public class QueueRSocketTest {
//    private static final Logger logger = (Logger) LoggerFactory.getLogger(QueueRSocketTest.class);

    @Autowired
    private RSocketRequester.Builder requesterBuilder;

    @Test
    void testQueueStatusRoute() {
        RSocketRequester requester = requesterBuilder
                .connectTcp("localhost", 7010)
                .block();

        QueueStatusRequest request = new QueueStatusRequest("test-user");

        Flux<QueueStatusResponse> response = requester
                .route("queue.status")
                .data(request)
                .retrieveFlux(QueueStatusResponse.class)
                .timeout(Duration.ofSeconds(5));  // 타임아웃을 5초로 늘려보세요.

        response
                .doOnNext(res -> System.out.println("✅ 응답: " + res))
                .blockLast(Duration.ofSeconds(3)); // 3초 안에 응답 없으면 timeout
    }
}