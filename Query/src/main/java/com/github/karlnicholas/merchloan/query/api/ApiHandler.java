package com.github.karlnicholas.merchloan.query.api;

import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class ApiHandler {
    private final RabbitMqSender rabbitMqSender;

    public ApiHandler(RabbitMqSender rabbitMqSender) {
        this.rabbitMqSender = rabbitMqSender;
    }

    public Mono<ServerResponse> getRequest(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(rabbitMqSender::queryServiceRequest)
                , String.class);
    }

    public Mono<ServerResponse> getAccount(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(rabbitMqSender::queryAccount)
                , String.class);
    }

    public Mono<ServerResponse> getLoan(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(rabbitMqSender::queryLoan)
                , String.class);
    }

    public Mono<ServerResponse> getStatement(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(rabbitMqSender::queryStatement)
                , String.class);
    }

    public Mono<ServerResponse> getStatements(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(rabbitMqSender::queryStatements)
                , String.class);
    }

    public Mono<ServerResponse> getCheckRequests(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.fromSupplier(()->rabbitMqSender.servicerequestCheckRequest())
                , Boolean.class);
    }
}
