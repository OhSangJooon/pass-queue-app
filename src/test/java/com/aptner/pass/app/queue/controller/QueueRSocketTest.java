package com.aptner.pass.app.queue.controller;

import com.aptner.pass.app.queue.model.QueueResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.logging.Logger;

@SpringBootTest
@AutoConfigureWebTestClient
public class QueueRSocketTest {
    @Autowired
    private RSocketRequester.Builder requesterBuilder;

    private RSocketRequester requester;

    @BeforeEach
    void setup() {
        this.requester = requesterBuilder
                .connectTcp("localhost", 7010)
                .block();
    }

    @Test
    void streamTest() {
        requester.route("queue.status")
                .retrieveFlux(QueueResponse.class)
                .take(5) // 예: 5개만 받고 종료
                .doOnNext(System.out::println)
                .blockLast(); // 실제 구독
    }
}