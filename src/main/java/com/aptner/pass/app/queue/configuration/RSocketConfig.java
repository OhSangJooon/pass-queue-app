//package com.aptner.pass.app.queue.configuration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.codec.json.Jackson2JsonDecoder;
//import org.springframework.http.codec.json.Jackson2JsonEncoder;
//import org.springframework.messaging.rsocket.RSocketRequester;
//import org.springframework.messaging.rsocket.RSocketStrategies;
//import org.springframework.util.MimeTypeUtils;
//import org.springframework.web.util.pattern.PathPatternRouteMatcher;
//import reactor.util.retry.Retry;
//
//import java.time.Duration;
//
//@Configuration
//public class RSocketConfig {
//    @Bean
//    public RSocketStrategies rSocketStrategies(ObjectMapper objectMapper) {
//        return RSocketStrategies.builder()
//                .decoder(new Jackson2JsonDecoder(objectMapper))
//                .encoder(new Jackson2JsonEncoder(objectMapper))
//                .routeMatcher(new PathPatternRouteMatcher())
//                .build();
//    }
//}
