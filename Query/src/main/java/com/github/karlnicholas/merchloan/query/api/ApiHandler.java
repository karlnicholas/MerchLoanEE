package com.github.karlnicholas.merchloan.query.api;

import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ApiHandler {
    private final RabbitMqSender rabbitMqSender;

    public ApiHandler(RabbitMqSender rabbitMqSender) {
        this.rabbitMqSender = rabbitMqSender;
    }

    public Mono<ServerResponse> getId(ServerRequest serverReqest) {
        return ServerResponse.ok().body(Mono.just(UUID.fromString(serverReqest.pathVariable("id")))
                .map(rabbitMqSender::queryServiceRequest), String.class);
    }

    public Mono<ServerResponse> getAccount(ServerRequest serverReqest) {
        return ServerResponse.ok().body(Mono.just(UUID.fromString(serverReqest.pathVariable("id")))
                .map(rabbitMqSender::queryAccount), String.class);
    }

    public Mono<ServerResponse> getLender(ServerRequest serverReqest) {
        return ServerResponse.ok().body(Mono.just(UUID.fromString(serverReqest.pathVariable("id")))
                .map(rabbitMqSender::queryLender), String.class);
    }

    public Mono<ServerResponse> getLenderLender(ServerRequest serverReqest) {
        return ServerResponse.ok().body(Mono.just(serverReqest.queryParam("lender").orElseThrow(() -> new IllegalArgumentException("lender query param not found")))
                .map(rabbitMqSender::queryLenderLender), String.class);
    }

    public Mono<ServerResponse> getLoan(ServerRequest serverReqest) {
        return ServerResponse.ok().body(Mono.just(UUID.fromString(serverReqest.pathVariable("id")))
                .map(rabbitMqSender::queryLoan), String.class);
    }

    public Mono<ServerResponse> getLedger(ServerRequest serverReqest) {
        return ServerResponse.ok().body(Mono.just(UUID.fromString(serverReqest.pathVariable("id")))
                .map(rabbitMqSender::queryLedger), String.class);
    }
}
